package com.foodmate.backend.service;

import com.foodmate.backend.dto.MemberDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface MemberService {

    MemberDto.Response getMemberInfo(Authentication authentication);
    Boolean checkDuplicateEmail(String email);

    String patchProfileImage(Authentication authentication, MultipartFile imageFile) throws IOException;

    Boolean checkDuplicateNickname(String nickname);

    String logoutMember(HttpServletRequest request, HttpServletResponse response);

    MemberDto.Response getMemberInfoByNickname(String nickname);

    String createMember(MemberDto.CreateMemberRequest request, MultipartFile imageFile) throws IOException;

    String createDefaultImageMember(MemberDto.CreateMemberRequest request);

    boolean emailAuth(String emailAuthKey);

    String toggleLikeForPost(Long memberId, Authentication authentication);
}
