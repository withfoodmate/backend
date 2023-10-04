package com.foodmate.backend.controller;

import com.foodmate.backend.dto.MemberDto;
import com.foodmate.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;


    /**
     * @param authentication 로그인한 사용자의 정보
     * @param imageFile 사용자가 업로드를 원하는 사진
     * 사용자에게 사진파일을 받아와 프로필 이미지 변경
     */
    @PatchMapping("/image")
    public ResponseEntity<String> patchProfileImage(Authentication authentication,
                                                    @RequestPart MultipartFile imageFile) throws IOException {
        return ResponseEntity.ok(memberService.patchProfileImage(authentication, imageFile));
    }


    /**
     * @param request 사용자가 입력한 email
     * @return 현재 사용중인 email이면 false
     *         아니면 true
     */
    @GetMapping("/email")
    public ResponseEntity<Boolean> checkDuplicateEmail(@RequestBody MemberDto.Request request){
        return ResponseEntity.ok(memberService.checkDuplicateEmail(request.getEmail()));
    }
}
