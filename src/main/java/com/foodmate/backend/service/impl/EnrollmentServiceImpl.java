package com.foodmate.backend.service.impl;

import com.foodmate.backend.dto.EnrollmentDto;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.EnrollmentStatus;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.EnrollmentRepository;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;

    @Override
    public Page<EnrollmentDto> getMyEnrollment(String status, Authentication authentication, Pageable pageable) {
        Member member = memberRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new MemberException(Error.USER_NOT_FOUND));
        LocalDateTime currentDate = LocalDateTime.now();
        log.error(member.getId().toString());
        return enrollmentRepository.getMyEnrollment(
                member.getId(),
                EnrollmentStatus.fromString(status),
                currentDate.minusMonths(3),
                currentDate.plusMonths(1),
                pageable);
    }
}
