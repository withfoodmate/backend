package com.foodmate.backend.repository;

import com.foodmate.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 모임 아이디로 채팅방 찾기
    Optional<ChatRoom> findByFoodGroupId(Long groupId);

    // 모임 아이디로 채팅방 삭제
    void deleteByFoodGroupId(Long groupId);


}
