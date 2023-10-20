package com.foodmate.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.foodmate.backend.entity.Reply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ReplyDto {

    @Getter
    @NotNull
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String content;
    }

    @Getter
    @Builder
    public static class Response {
        private Long replyId;
        private Long memberId;
        private String nickname;
        private String image;
        private String content;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime updatedDate;

        public static Response createReplyResponse(Reply reply) {
            return Response.builder()
                    .replyId(reply.getId())
                    .memberId(reply.getMember().getId())
                    .nickname(reply.getMember().getNickname())
                    .image(reply.getMember().getImage())
                    .content(reply.getContent())
                    .createdDate(reply.getCreatedDate())
                    .updatedDate(reply.getUpdatedDate())
                    .build();
        }
    }

}
