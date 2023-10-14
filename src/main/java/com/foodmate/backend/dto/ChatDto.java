package com.foodmate.backend.dto;

import com.foodmate.backend.entity.ChatMessage;
import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.FoodGroup;
import com.foodmate.backend.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ChatDto {

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatRoomListResponse{
        private Long chatRoomId;
        private String chatRoomName;
        private String lastMessage;
        private LocalDateTime lastMessageTime;
        private int count;


        public static ChatDto.ChatRoomListResponse createChatRoomListResponse(
                ChatRoom chatRoom, FoodGroup foodGroup, ChatMessage chatMessage, int count){
            return ChatRoomListResponse.builder()
                    .chatRoomId(chatRoom.getId())
                    .chatRoomName(foodGroup.getName())
                    .lastMessage(chatMessage == null ? null : chatMessage.getContent())
                    .lastMessageTime(chatMessage == null ? null : chatMessage.getCreateDateTime())
                    .count(count)
                    .build();
        }

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class ChatMember{
        private Long memberId;
        private String nickname;
        private String image;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatRoomInfoResponse{
        private Long chatRoomId;
        private String chatRoomName;
        private LocalDateTime groupDate;
        private int count;
        private List<ChatMember> chatMembers;

        public static ChatDto.ChatRoomInfoResponse createChatRoomInfo(ChatRoom chatRoom, FoodGroup foodGroup, List<Member> members) {
            List<ChatMember> chatMemberList = new ArrayList<>();
            for(Member member : members){
                chatMemberList.add(
                        new ChatMember(member.getId(), member.getNickname(), member.getImage())
                );
            }

            return ChatRoomInfoResponse.builder()
                    .chatRoomId(chatRoom.getId())
                    .chatRoomName(foodGroup.getName())
                    .groupDate(foodGroup.getGroupDateTime())
                    .count(chatRoom.getAttendance())
                    .chatMembers(chatMemberList)
                    .build();
        }

    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatRoomMessageResponse {
        private Long memberId;
        private String nickname;
        private String image;
        private String content;
        private LocalDateTime createdDate;

        public static ChatRoomMessageResponse createChatMessageListResponse(
                ChatMessage chatMessage, Member member) {
            return ChatRoomMessageResponse.builder()
                    .memberId(member.getId())
                    .nickname(member.getNickname())
                    .image(member.getImage())
                    .content(chatMessage.getContent())
                    .createdDate(chatMessage.getCreateDateTime())
                    .build();
        }
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatRoomMessageListResponse {
        private Long loginMemberId;
        private List<ChatRoomMessageResponse> chatRoomMessageResponses;

        public static ChatRoomMessageListResponse createChatMessageAllListResponse(
                Long loginMemberId, List<ChatRoomMessageResponse> chatRoomMessageResponseList) {
            return ChatRoomMessageListResponse.builder()
                    .loginMemberId(loginMemberId)
                    .chatRoomMessageResponses(chatRoomMessageResponseList)
                    .build();
        }
    }
}
