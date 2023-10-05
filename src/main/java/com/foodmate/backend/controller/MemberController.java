package com.foodmate.backend.controller;

import com.foodmate.backend.dto.MemberDto;
import com.foodmate.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;


    /**
     * @param authentication 로그인한 사용자의 정보
     * @return 사용자 본인 정보 조회
     */
    @GetMapping
    public ResponseEntity<MemberDto.Response> getMemberInfo(Authentication authentication){
        return ResponseEntity.ok(memberService.getMemberInfo(authentication));
    }


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


    /**
     * @param request 사용자가 입력한 nickname
     * @return 현재 사용중인 nickname이면 false
     *         아니면 true
     */
    @GetMapping("/nickname")
    public ResponseEntity<Boolean> checkDuplicateNickname(@RequestBody MemberDto.Request request){
        return ResponseEntity.ok(memberService.checkDuplicateNickname(request.getNickname()));
    }

    /**
     * @param request 로그아웃을 위한
     * @param response 로그아웃을 위한
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logoutMember(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(memberService.logoutMember(request, response));
    }

    /**
     * @param nickname
     * @return 다른 사람의 사용자 정보 가져오기
     */
    @GetMapping("/{nickname}")
    public ResponseEntity<MemberDto.Response> getMemberInfoByNickname(@PathVariable String nickname){
        return ResponseEntity.ok(memberService.getMemberInfoByNickname(nickname));
    }

    /**
     * @param request 회원가입 (사용자가 입력한 정보)
     * @param imageFile 사용자가 등록한 프로필 이미지파일(null 허용)
     * @return
     * @throws IOException 사진 업로드 실패시
     */
    @PostMapping("/signup")
    public ResponseEntity<String> createMember(@RequestPart MemberDto.CreateMemberRequest request,
                                               @RequestPart(value = "file", required = false) MultipartFile imageFile) throws IOException {

        if(imageFile == null){ // 이미지가 없을 때
            return ResponseEntity.ok(memberService.createDefaultImageMember(request));
        }
        return ResponseEntity.ok(memberService.createMember(request, imageFile)); // 이미지가 있을 때
    }

    /**
     * @param emailAuthKey 이메일 인증 키
     * @return
     */
    @GetMapping("/email-auth")
    public boolean emailAuth (@RequestParam("id") String emailAuthKey){
        boolean result = memberService.emailAuth(emailAuthKey);
        return result;
    }
}
