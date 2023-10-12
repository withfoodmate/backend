package com.foodmate.backend.dto;

import com.foodmate.backend.entity.ChatMessage;
import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.FoodGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


public class ChatDto {

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatRoomListResponse{
        private Long chatRoomId;
        private String chatRoomName;
        private String content;
        private LocalDateTime lastMessageTime;
        private Integer count;


        public static ChatDto.ChatRoomListResponse createChatRoomListResponse(
                ChatRoom chatRoom, FoodGroup foodGroup, ChatMessage chatMessage, Integer count){
            return ChatRoomListResponse.builder()
                    .chatRoomId(chatRoom.getId())
                    .chatRoomName(foodGroup.getName())
                    .content(chatMessage.getContent())
                    .lastMessageTime(chatMessage.getCreateDateTime())
                    .count(count)
                    .build();
        }

    }
}
