package com.foodmate.backend.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MemberService {

    Boolean checkDuplicateEmail(String nickname);
    String patchProfileImage(Authentication authentication, MultipartFile imageFile) throws IOException;
}
