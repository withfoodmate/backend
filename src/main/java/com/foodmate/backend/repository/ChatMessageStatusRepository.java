package com.foodmate.backend.repository;

import com.foodmate.backend.entity.ChatMessageStatus;
import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageStatusRepository extends JpaRepository<ChatMessageStatus, Long> {
    Integer countByChatMessage_ChatRoomAndMessageStatusAndMember(ChatRoom chatRoom, MessageStatus messageStatus, Member member);
}
