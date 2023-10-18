package com.foodmate.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.foodmate.backend.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class ChatMessageDto {

    @Getter
    public static class Request {
        private String content;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdDate;   // 발신 시간
    }

    @Getter
    @Builder
    public static class Response {
        private Long memberId;               // 발신자 ID
        private String nickname;             // 발신자 닉네임
        private String image;                // 발신자 프로필 이미지 경로
        private String content;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdDate;   // 발신 시간

        public static Response createChatMessageResponse(Member member, Request request) {
            return ChatMessageDto.Response.builder()
                    .memberId(member.getId())
                    .nickname(member.getNickname())
                    .image(member.getImage())
                    .content(request.getContent())
                    .createdDate(request.getCreatedDate())
                    .build();
        }
    }

}
