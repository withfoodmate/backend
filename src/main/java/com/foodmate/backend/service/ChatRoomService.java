package com.foodmate.backend.service;

import com.foodmate.backend.dto.ChatDto;
import com.foodmate.backend.entity.*;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.ChatException;
import com.foodmate.backend.exception.FoodException;
import com.foodmate.backend.exception.GroupException;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    private final FoodGroupRepository foodGroupRepository;
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
            ChatRoom chatRoom = chatRoomRepository.findById(chatMember.getChatRoom().getId()).orElseThrow(
                    () -> new FoodException(Error.CHATROOM_NOT_FOUND)
            );

            FoodGroup foodGroup = foodGroupRepository.findById(chatRoom.getId()).orElseThrow(
                    () -> new GroupException(Error.GROUP_NOT_FOUND)
            );

            Optional<ChatMessage> chatMessage = chatMessageRepository
                    .findTopByChatRoomAndCreateDateTimeAfterOrderByCreateDateTimeDesc(chatRoom, chatMember.getInsertTime());

            chatRoomListResponses.add(ChatDto.ChatRoomListResponse.createChatRoomListResponse(
                    chatRoom, foodGroup, chatMessage.isEmpty() ? null : chatMessage.get(),
                    chatMessageRepository.countByCreateDateTimeAfterAndChatRoom(chatMember.getLastReadTime(), chatRoom)
            ));
        }

        chatRoomListResponses.sort(Comparator.comparing(ChatDto.ChatRoomListResponse::getLastMessageTime).reversed()); // 최신순 정렬
        return chatRoomListResponses;

    }

    public ChatDto.ChatRoomInfoResponse getChatRoomInfo(Long charRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(charRoomId).orElseThrow(
                () -> new ChatException(Error.CHATROOM_NOT_FOUND)
        );

        FoodGroup foodGroup = foodGroupRepository.findById(chatRoom.getFoodGroup().getId()).orElseThrow(
                () -> new GroupException(Error.GROUP_NOT_FOUND)
        );

        List<ChatMember> chatMembers = chatMemberRepository.findByChatRoom(chatRoom);
        List<Member> members = new ArrayList<>();

        for(ChatMember chatMember : chatMembers){
            Member member = memberRepository.findById(chatMember.getMember().getId()).orElseThrow(
                    () -> new MemberException(Error.USER_NOT_FOUND)
            );
            if(member.getImage() == null) {
                member.setImage(defaultProfileImage);
            }
            members.add(member);
        }
        return ChatDto.ChatRoomInfoResponse.createChatRoomInfo(chatRoom, foodGroup, members);
    }
}
