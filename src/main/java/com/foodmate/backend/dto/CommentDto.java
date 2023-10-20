package com.foodmate.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.foodmate.backend.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class CommentDto {

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
        private Long commentId;
        private Long memberId;
        private String nickname;
        private String image;
        private String content;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime updatedDate;
        private List<ReplyDto.Response> replies;

        public static Response createCommentResponse(Comment comment, List<ReplyDto.Response> replies) {
            return Response.builder()
                    .commentId(comment.getId())
                    .memberId(comment.getMember().getId())
                    .nickname(comment.getMember().getNickname())
                    .image(comment.getMember().getImage())
                    .content(comment.getContent())
                    .createdDate(comment.getCreatedDate())
                    .updatedDate(comment.getUpdatedDate())
                    .replies(replies)
                    .build();
        }
    }

}
