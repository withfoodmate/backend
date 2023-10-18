package com.foodmate.backend.service;

import com.foodmate.backend.dto.ChatMessageDto;
import com.foodmate.backend.entity.ChatMessage;
import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.ChatException;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.ChatMessageRepository;
import com.foodmate.backend.repository.ChatRoomRepository;
import com.foodmate.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final String PREFIX = "/topic/chatroom/";

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageDto.Response saveMessage(Long chatRoomId, Long memberId, ChatMessageDto.Request request) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(Error.CHATROOM_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        chatMessageRepository.save(ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(member)
                .content(request.getContent())
                .createDateTime(request.getCreatedDate())
                .build());

        return ChatMessageDto.Response.createChatMessageResponse(member, request);
    }

    public void incrementAttendance(String destination) {
        ChatRoom chatRoom = getChatRoom(destination);
        chatRoom.setAttendance(chatRoom.getAttendance() + 1);
        chatRoomRepository.save(chatRoom);
    }

    public void decrementAttendance(String destination) {
        ChatRoom chatRoom = getChatRoom(destination);
        chatRoom.setAttendance(chatRoom.getAttendance() - 1);
        chatRoomRepository.save(chatRoom);
    }

    private ChatRoom getChatRoom(String destination) {
        Long chatRoomId = Long.parseLong(
                destination.substring(PREFIX.length()));

        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(Error.CHATROOM_NOT_FOUND));
    }

}
