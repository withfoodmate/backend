package com.foodmate.backend.service.impl;

import com.foodmate.backend.dto.EnrollmentDto;
import com.foodmate.backend.entity.Enrollment;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.EnrollmentStatus;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.EnrollmentException;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.EnrollmentRepository;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Override
    public Page<EnrollmentDto.RequestList> enrollmentList(String decision, Authentication authentication, Pageable pageable) {

        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        Page<Enrollment> enrollmentsPage;

        if (decision.equals("processed")) {
            enrollmentsPage = enrollmentRepository.findByMyEnrollmentProcessedList(member.getId(), pageable);
            return enrollmentsPage.map(enrollment -> EnrollmentDto.RequestList.builder()
                    .enrollmentId(enrollment.getId())
                    .groupId(enrollment.getFoodGroup().getId())
                    .memberId(enrollment.getMember().getId())
                    .nickname(enrollment.getMember().getNickname())
                    .image(enrollment.getMember().getImage())
                    .title(enrollment.getFoodGroup().getTitle())
                    .name(enrollment.getFoodGroup().getName())
                    .food(enrollment.getFoodGroup().getFood().getType())
                    .date(enrollment.getFoodGroup().getGroupDateTime().toLocalDate())
                    .time(enrollment.getFoodGroup().getGroupDateTime().toLocalTime())
                    .maximum(enrollment.getFoodGroup().getMaximum())
                    .storeName(enrollment.getFoodGroup().getStoreName())
                    .storeAddress(enrollment.getFoodGroup().getStoreAddress())
                    .build());

        } else if (decision.equals("unprocessed")) {
            enrollmentsPage = enrollmentRepository.findByMyEnrollmentUnprocessedList(member.getId(), pageable);
            return enrollmentsPage.map(enrollment -> EnrollmentDto.RequestList.builder()
                    .enrollmentId(enrollment.getId())
                    .memberId(enrollment.getMember().getId())
                    .nickname(enrollment.getMember().getNickname())
                    .image(enrollment.getMember().getImage())
                    .build());
        } else {
            throw new EnrollmentException(Error.REQUEST_NOT_FOUND);
        }


    }

    @Override
    public String acceptEnrollment(Long enrollmentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentException(Error.ENROLLMENT_NOT_FOUND));
        enrollment.updateEnrollment(EnrollmentStatus.ACCEPT);
        enrollmentRepository.save(enrollment);

        return "수락 완료";
    }

    @Override
    public String refuseEnrollment(Long enrollmentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentException(Error.ENROLLMENT_NOT_FOUND));
        enrollment.updateEnrollment(EnrollmentStatus.REFUSE);
        enrollmentRepository.save(enrollment);

        return "거절 완료";
    }
}

