package com.foodmate.backend.service;

import com.foodmate.backend.dto.ChatDto;
import com.foodmate.backend.entity.*;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.enums.MessageStatus;
import com.foodmate.backend.exception.ChatException;
import com.foodmate.backend.exception.FoodException;
import com.foodmate.backend.exception.GroupException;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageStatusRepository chatMessageStatusRepository;

    private final FoodGroupRepository foodGroupRepository;
    private final MemberRepository memberRepository;


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
                    chatRoom, foodGroup, chatMessage.isEmpty() ? new ChatMessage() : chatMessage.get(),
                    chatMessageStatusRepository.countByChatMessage_ChatRoomAndMessageStatusAndMember(chatRoom, MessageStatus.UNREAD, member)
            ));
            }
        return chatRoomListResponses;

    }
}
