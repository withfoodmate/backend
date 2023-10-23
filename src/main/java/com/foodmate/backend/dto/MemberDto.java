package com.foodmate.backend.dto;

import com.foodmate.backend.entity.Member;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
public class MemberDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class emailRequest{
        private String email;
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class nicknameRequest{
        private String nickname;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class loginRequest{
        private String email;
        private String password;
    }
    @Builder
    @Getter
    public static class CreateMemberRequest{
        private String email;
        private String nickname;
        private String password;
        private List<String> food;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class passwordUpdateRequest{
        private String oldPassword;
        private String newPassword;
    }


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class deleteMemberRequest{
        private String password;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class changePreferenceFoodRequest{
        List<String> food;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Response{
        private Long memberId;
        private String email;
        private String nickname;
        private String image;
        private Long likes;
        List<String> food;

        public static MemberDto.Response createMemberDtoResponse(Member member, List<String> food){
            return Response.builder()
                    .memberId(member.getId())
                    .email(member.getEmail())
                    .nickname(member.getNickname())
                    .image(member.getImage())
                    .likes(member.getLikes())
                    .food(food)
                    .build();
        }

        public static MemberDto.Response createMemberDtoResponse(Member member,  List<String> food, String defaultImage){
            return Response.builder()
                    .memberId(member.getId())
                    .email(member.getEmail())
                    .nickname(member.getNickname())
                    .image(defaultImage)
                    .likes(member.getLikes())
                    .food(food)
                    .build();
        }
    }


}
