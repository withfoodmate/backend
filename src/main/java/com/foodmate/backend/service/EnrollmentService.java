package com.foodmate.backend.service;

import com.foodmate.backend.dto.EnrollmentDto;
import com.foodmate.backend.entity.Enrollment;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.EnrollmentStatus;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.EnrollmentException;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.EnrollmentRepository;
import com.foodmate.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;

//    @Override
//    public Page<EnrollmentDto> getMyEnrollment(String status, Authentication authentication, Pageable pageable) {
//        Member member = memberRepository.findByEmail(authentication.getName()).orElseThrow(
//                () -> new MemberException(Error.USER_NOT_FOUND));
//        LocalDateTime currentDate = LocalDateTime.now();
//        log.error(member.getId().toString());
//        return enrollmentRepository.getMyEnrollment(
//                member.getId(),
//                EnrollmentStatus.fromString(status),
//                currentDate.minusMonths(3),
//                currentDate.plusMonths(1),
//                pageable);
//    }


    public Page<EnrollmentDto.myEnrollmentResponse> getMyEnrollment(String status, Authentication authentication, Pageable pageable) {
        // 현재 시간 가져오기
        LocalDateTime currentDate = LocalDateTime.now();

        // 사용자 정보 조회
        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));
        // 정렬 정보를 포함한 pageable 객체 생성
        Pageable pageableWithSorting = PageRequest.of(
                pageable.getPageNumber(), // 현재 페이지 번호
                pageable.getPageSize(),   // 페이지 크기
                Sort.by(Sort.Order.asc("foodGroupGroupDateTime")) // 정렬 정보
        );


        // 해당 사용자의 신청 정보 페이징 조회
        Page<EnrollmentDto.myEnrollmentResponse> enrollmentPage = enrollmentRepository.findByMemberAndStatusAndFoodGroupGroupDateTimeBetween(
                member,
                EnrollmentStatus.fromString(status),
                currentDate.minusMonths(3),
                currentDate.plusMonths(1),
                pageableWithSorting); // 정렬 정보를 포함한 pageable 사용

        return enrollmentPage;
    }


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


    public String acceptEnrollment(Long enrollmentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentException(Error.ENROLLMENT_NOT_FOUND));
        enrollment.updateEnrollment(EnrollmentStatus.ACCEPT);
        enrollmentRepository.save(enrollment);

        return "수락 완료";
    }


    public String refuseEnrollment(Long enrollmentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentException(Error.ENROLLMENT_NOT_FOUND));
        enrollment.updateEnrollment(EnrollmentStatus.REFUSE);
        enrollmentRepository.save(enrollment);

        return "거절 완료";
    }


    public String cancelEnrollment(Long enrollmentId, Authentication authentication) {
        Member member = memberRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new MemberException(Error.USER_NOT_FOUND));

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(
                () -> new EnrollmentException(Error.ENROLLMENT_NOT_FOUND));

        log.error(enrollment.getId().toString());
        if(enrollment.getMember().getId() != member.getId()){
            throw new EnrollmentException(Error.ACCESS_DENIED);
        }


        if(enrollment.getStatus() != EnrollmentStatus.SUBMIT){
            throw new EnrollmentException(Error.ENROLLMENT_CANCEL_NOT_STATUS);
        }
        enrollment.setStatus(EnrollmentStatus.CANCEL);
        enrollmentRepository.save(enrollment);

        return "취소 완료";
    }
}
