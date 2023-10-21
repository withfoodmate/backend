package com.foodmate.backend.service;

import com.foodmate.backend.dto.ChatMessageDto;
import com.foodmate.backend.entity.ChatMember;
import com.foodmate.backend.entity.ChatMessage;
import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.ChatException;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.ChatMemberRepository;
import com.foodmate.backend.repository.ChatMessageRepository;
import com.foodmate.backend.repository.ChatRoomRepository;
import com.foodmate.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final String PREFIX = "/topic/chatroom/";

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMemberRepository chatMemberRepository;

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

    @Transactional
    public void incrementAttendance(String destination, String nickname) {
        ChatRoom chatRoom = getChatRoom(destination);

        Member member = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        Optional<ChatMember> optionalChatMember = chatMemberRepository.findByMemberAndChatRoom(member, chatRoom);

        if (optionalChatMember.isPresent()) {
            ChatMember chatMember = optionalChatMember.get();
            chatMember.setLastReadTime(LocalDateTime.now());
            chatMemberRepository.save(chatMember);
        } else {
            // ChatMember 에 현재 멤버 추가
            chatMemberRepository.save(ChatMember.builder()
                    .member(member)
                    .chatRoom(chatRoom)
                    .lastReadTime(LocalDateTime.now())
                    .build());

            // ChatRoom 의 현재인원 +1
            chatRoom.setAttendance(chatRoom.getAttendance() + 1);
            chatRoomRepository.save(chatRoom);
        }
    }

    @Transactional
    public void decrementAttendance(String destination, String nickname) {
        ChatRoom chatRoom = getChatRoom(destination);

        Member member = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        // ChatMember 에서 현재 멤버 삭제
        chatMemberRepository.deleteByMemberAndChatRoom(member, chatRoom);

        // ChatRoom 의 현재인원 -1
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
