package com.foodmate.backend.repository;

import com.foodmate.backend.entity.ChatMember;
import com.foodmate.backend.entity.ChatMessage;
import com.foodmate.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {

    Optional<ChatMessage> findTopByChatRoomAndCreateDateTimeAfterOrderByCreateDateTimeDesc(ChatRoom chatRoom, LocalDateTime insertTime);

    int countByCreateDateTimeAfterAndChatRoom(LocalDateTime lastReadTime, ChatRoom chatRoom);
}
