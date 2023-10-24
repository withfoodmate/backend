package com.foodmate.backend.service;

import com.foodmate.backend.dto.EnrollmentDto;
import com.foodmate.backend.entity.Enrollment;
import com.foodmate.backend.entity.FoodGroup;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.EnrollmentStatus;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.EnrollmentException;
import com.foodmate.backend.exception.GroupException;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.EnrollmentRepository;
import com.foodmate.backend.repository.FoodGroupRepository;
import com.foodmate.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;
    private final FoodGroupRepository foodGroupRepository;

    @Value("${S3_GENERAL_IMAGE_PATH}")
    private String defaultProfileImage;

    public Page<EnrollmentDto.myEnrollmentResponse> getMyEnrollment(Authentication authentication, Pageable pageable) {
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

        return enrollmentRepository.findByMemberAndStatusInAndFoodGroupGroupDateTimeBetween(
                member,
                List.of(EnrollmentStatus.SUBMIT, EnrollmentStatus.CANCEL, EnrollmentStatus.ACCEPT, EnrollmentStatus.REFUSE),
                currentDate,
                currentDate.plusMonths(1),
                pageableWithSorting); // 정렬 정보를 포함한 pageable 사용
    }

    public Page<EnrollmentDto.myEnrollmentResponse> getMyEnrollmentHistory(Authentication authentication, Pageable pageable) {
        LocalDateTime currentDate = LocalDateTime.now();

        // 사용자 정보 조회
        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));
        // 정렬 정보를 포함한 pageable 객체 생성
        Pageable pageableWithSorting = PageRequest.of(
                pageable.getPageNumber(), // 현재 페이지 번호
                pageable.getPageSize(),   // 페이지 크기
                Sort.by(Sort.Order.desc("foodGroupGroupDateTime")) // 정렬 정보
        );

        // 해당 사용자의 신청 정보 페이징 조회
        return enrollmentRepository.findByMemberAndStatusInAndFoodGroupGroupDateTimeBetween(
                member,
                List.of(EnrollmentStatus.GROUP_COMPLETE, EnrollmentStatus.GROUP_CANCEL),
                currentDate.minusMonths(3),
                currentDate,
                pageableWithSorting);
    }

    public Page<EnrollmentDto.myEnrollmentResponse> getMyAllEnrollment(Authentication authentication, Pageable pageable) {
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

        Page<EnrollmentDto.myEnrollmentResponse> enrollmentPage = enrollmentRepository.findByMemberAndFoodGroupGroupDateTimeBetween(
                member,
                currentDate.minusMonths(3),
                currentDate.plusMonths(1),
                pageableWithSorting);

        for(EnrollmentDto.myEnrollmentResponse response : enrollmentPage) {
            if(response.getFoodGroupMemberImage() == null) {
                response.setFoodGroupMemberImage(defaultProfileImage);
            }
        }

        return enrollmentPage;
    }


    public Page<EnrollmentDto.myEnrollmentReceiveResponse> enrollmentList(String decision, Authentication authentication, Pageable pageable) {

        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        Page<Enrollment> enrollmentsPage;

        if (decision.equals("processed")) {
            enrollmentsPage = enrollmentRepository.findByMyEnrollmentProcessedListWithStatus(member.getId(), EnrollmentStatus.ACCEPT, pageable);
        } else if (decision.equals("unprocessed")) {
            enrollmentsPage = enrollmentRepository.findByMyEnrollmentProcessedListWithStatus(member.getId(), EnrollmentStatus.SUBMIT, pageable);
        } else {
            throw new EnrollmentException(Error.REQUEST_NOT_FOUND);
        }
        return enrollmentsPage.map(EnrollmentDto.myEnrollmentReceiveResponse::createMyEnrollmentReceiveResponse);


    }

    public Page<EnrollmentDto.myReceiveEnrollmentResponse> allEnrollmentList(Authentication authentication, Pageable pageable) {

        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        Page<EnrollmentDto.myReceiveEnrollmentResponse>  enrollmentsPage = enrollmentRepository.findByFoodGroupMember(member, pageable);

        for(EnrollmentDto.myReceiveEnrollmentResponse response : enrollmentsPage) {
            if(response.getMemberImage() == null) {
                response.setMemberImage(defaultProfileImage);
            }
        }
        return enrollmentsPage;


    }

    public Enrollment acceptEnrollment(Long enrollmentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentException(Error.ENROLLMENT_NOT_FOUND));
        enrollment.updateEnrollmentStatus(EnrollmentStatus.ACCEPT);
        FoodGroup foodGroup = foodGroupRepository.findById(enrollment.getFoodGroup().getId())
                .orElseThrow(() -> new GroupException(Error.GROUP_NOT_FOUND));
        foodGroup.updateEnrollmentAttendance(foodGroup.getAttendance() + 1);
        enrollmentRepository.save(enrollment);

        return enrollment;
    }


    public Enrollment refuseEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentException(Error.ENROLLMENT_NOT_FOUND));
        enrollment.updateEnrollmentStatus(EnrollmentStatus.REFUSE);
        enrollmentRepository.save(enrollment);
        return enrollment;
    }


    public void cancelEnrollment(Long enrollmentId, Authentication authentication) {
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
    }



}
