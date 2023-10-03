package com.foodmate.backend.service.impl;

import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    /**
     * @param email
     * @return 현재 사용중인 email 이면 false 리턴
     *         아니면 true
     */
    @Override
    public Boolean checkDuplicateEmail(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            return false;
        }
        return true;
    }
}
