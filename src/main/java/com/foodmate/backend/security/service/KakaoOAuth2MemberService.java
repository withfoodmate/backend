package com.foodmate.backend.security.service;


import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.enums.MemberLoginType;
import com.foodmate.backend.enums.MemberRole;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class KakaoOAuth2MemberService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService userService;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        //기존 DefaultOAuth2UserService 로직
        OAuth2User oAuth2User = userService.loadUser(userRequest);

        //로그인 한 이메일 정보 가져오기
        Map<String, Object> memberInfo = oAuth2User.getAttribute("kakao_account");
        String email = (String) memberInfo.get("email");
        log.info(email);
        if(isDeleteMember(email)){
            throw new MemberException(Error.DELETED_USER);
        }
        saveUser(email);

        return oAuth2User;
    }

    /**
     *
     * @param email
     * @return
     * 이미 탈퇴한 기록이 있으면 true
     * 아니면 false
     */
    private boolean isDeleteMember(String email) {
        Optional<Member> memberOptional = memberRepository.findByEmail(email);
        return memberOptional.isPresent() && memberOptional.get().getIsDeleted() != null;
    }

    private void saveUser(String email) {
        Optional<Member> memberOptional = memberRepository.findByEmail(email);

        if (memberOptional.isEmpty()) {
            memberRepository.save(Member.createKakaolMember(email));
        }
    }


}