package com.foodmate.backend.service;

import com.foodmate.backend.dto.EnrollmentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface EnrollmentService {

    Page<EnrollmentDto> getMyEnrollment(String status, Authentication authentication, Pageable pageable);

    Page<EnrollmentDto.RequestList> enrollmentList(String decision, Authentication authentication, Pageable pageable);

    String acceptEnrollment(Long enrollmentId);

    String refuseEnrollment(Long enrollmentId);
}
