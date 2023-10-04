package com.foodmate.backend.service;

import com.foodmate.backend.dto.MemberDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MemberService {

    MemberDto.Response getMemberInfo(Authentication authentication);
    Boolean checkDuplicateEmail(String email);

    String patchProfileImage(Authentication authentication, MultipartFile imageFile) throws IOException;

    Boolean checkDuplicateNickname(String nickname);
}
