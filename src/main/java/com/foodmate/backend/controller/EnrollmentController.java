package com.foodmate.backend.controller;

import com.foodmate.backend.dto.EnrollmentDto;
import com.foodmate.backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/enrollment")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    /**
     * @param status // 신청상태
     * @param authentication // 사용자 정보
     * @param pageable
     * @return 신청상태에 맞는 신청정보를 가져옴
     */
    @GetMapping("")
    public ResponseEntity<Page<EnrollmentDto>> getMyEnrollment(@RequestParam String status, Authentication authentication, Pageable pageable){
        return ResponseEntity.ok(enrollmentService.getMyEnrollment(status, authentication, pageable));
    }

    /**
     *
     * @param decision 처리 상태
     * @param authentication 사용자 정보
     * @return 처리 상태에 따른 정보 가져옴
     */
    @GetMapping("/receive")
    public ResponseEntity<Page<EnrollmentDto.RequestList>> enrollmentList(@RequestParam String decision, Authentication authentication) {
        return ResponseEntity.ok(enrollmentService.enrollmentList(decision, authentication));
    }
}
