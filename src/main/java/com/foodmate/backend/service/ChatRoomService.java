package com.foodmate.backend.service;

import com.foodmate.backend.dto.ChatDto;
import com.foodmate.backend.entity.ChatMember;
import com.foodmate.backend.entity.ChatMessage;
import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.ChatException;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    @Value("${S3_GENERAL_IMAGE_PATH}")
    private String defaultProfileImage;

    public List<ChatDto.ChatRoomListResponse> getChatRoomList(Authentication authentication) {
        Member member = memberRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new MemberException(Error.USER_NOT_FOUND)
        );
        List<ChatMember>  chatMembers = chatMemberRepository.findByMember(member);

        if(chatMembers == null || chatMembers.isEmpty()){
            throw new ChatException(Error.USER_NOT_IN_CHATROOM);
        }
        List<ChatDto.ChatRoomListResponse> chatRoomListResponses = new ArrayList<>();
        for(ChatMember chatMember : chatMembers) {
            Optional<ChatMessage> chatMessage = chatMessageRepository
                    .findTopByChatRoomAndCreateDateTimeAfterOrderByCreateDateTimeDesc(chatMember.getChatRoom(), chatMember.getInsertTime());
            if(chatMessage.isEmpty()){
                chatRoomListResponses.add(ChatDto.ChatRoomListResponse.createChatRoomListResponse(
                        chatMember.getChatRoom(), chatMember.getChatRoom().getFoodGroup(), "", chatMember.getInsertTime(),
                        chatMessageRepository.countByCreateDateTimeAfterAndChatRoom(chatMember.getLastReadTime(), chatMember.getChatRoom())
                ));
            }else {
                chatRoomListResponses.add(ChatDto.ChatRoomListResponse.createChatRoomListResponse(
                        chatMember.getChatRoom(), chatMember.getChatRoom().getFoodGroup(), chatMessage.get().getContent(), chatMessage.get().getCreateDateTime(),
                        chatMessageRepository.countByCreateDateTimeAfterAndChatRoom(chatMember.getLastReadTime(), chatMember.getChatRoom())
                ));
            }
        }

        chatRoomListResponses.sort(Comparator.comparing(ChatDto.ChatRoomListResponse::getLastMessageTime).reversed()); // 최신순 정렬
        return chatRoomListResponses;

    }

    public ChatDto.ChatRoomInfoResponse getChatRoomInfo(Long charRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(charRoomId).orElseThrow(
                () -> new ChatException(Error.CHATROOM_NOT_FOUND)
        );
        List<ChatMember> chatMembers = chatMemberRepository.findByChatRoom(chatRoom);
        List<ChatDto.ChatMemberInfo> chatMemberInfos = new ArrayList<>();

        for(ChatMember chatMember : chatMembers){
            Member member = chatMember.getMember();
            if(member.getImage() == null) {
                member.setImage(defaultProfileImage);
            }
            chatMemberInfos.add(ChatDto.ChatMemberInfo.createChatMemberInfo(member));
        }
        return ChatDto.ChatRoomInfoResponse.createChatRoomInfo(chatRoom, chatRoom.getFoodGroup(), chatMemberInfos);
    }


    public ChatDto.ChatRoomMessageListResponse getChatRoomMessageList(Authentication authentication, Long chatRoomId) {
        chatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new ChatException(Error.CHATROOM_NOT_FOUND)
        );

        Member loginMember = memberRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new MemberException(Error.USER_NOT_FOUND)
        );

        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoom_Id(chatRoomId);
        List<ChatDto.ChatRoomMessageResponse> chatRoomMessageResponseList = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            Member otherMember = memberRepository.findById(chatMessage.getMember().getId()).orElseThrow(
                    () -> new MemberException(Error.USER_NOT_FOUND)
            );
            if(otherMember.getImage() == null) {
                otherMember.setImage(defaultProfileImage);
            }

            chatRoomMessageResponseList.add(ChatDto.ChatRoomMessageResponse.createChatMessageListResponse(chatMessage, otherMember));
        }

        return ChatDto.ChatRoomMessageListResponse.createChatMessageAllListResponse(loginMember.getId(), chatRoomMessageResponseList);
    }
}
