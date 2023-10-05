package com.foodmate.backend.service;

import com.foodmate.backend.dto.MemberDto;
import com.foodmate.backend.security.dto.JwtTokenDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

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

    JwtTokenDto login(Map<String, String> loginInfo);

    String changePassword(MemberDto.passwordUpdateRequest request, Authentication authentication);

    String deleteKakaoMember(HttpServletRequest request, HttpServletResponse response, Authentication authentication);

    String deleteGeneralMember(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication,
            MemberDto.deleteMemberRequest deleteMemberRequest);


    String changePreferenceFood(MemberDto.changePreferenceFoodRequest request, Authentication authentication);
}
