package com.foodmate.backend.controller;

import com.foodmate.backend.dto.EnrollmentDto;
import com.foodmate.backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/enrollment")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    /**
     * @param status // 신청상태
     * @param authentication // 사용자 정보
     * @param
     * @return 신청상태에 맞는 신청정보를 가져옴
     */
    @GetMapping("")
    public ResponseEntity<Page<EnrollmentDto.myEnrollmentResponse>> getMyEnrollment(
            @RequestParam String status,
            Authentication authentication,
           @PageableDefault(size = 5) Pageable pageable
    ) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollment(status, authentication, pageable));
    }

    /**
     *
     * @param decision 처리 상태
     * @param authentication 사용자 정보
     * @return 처리 상태에 따른 정보 가져옴
     */
    @GetMapping("/receive")
    public ResponseEntity<Page<EnrollmentDto.RequestList>> enrollmentList(@RequestParam String decision, Authentication authentication, Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.enrollmentList(decision, authentication, pageable));
    }

    /**
     * 모임 요청 수락
     * @param enrollmentId 신청 id
     * @return 처리 상태에 대한 응답
     */
    @PatchMapping("/{enrollmentId}/accept")
    public ResponseEntity<Void> acceptEnrollment(@PathVariable Long enrollmentId) {
        enrollmentService.acceptEnrollment(enrollmentId);
        return ResponseEntity.ok().build();
    }

    /**
     * 모임 요청 거절
     * @param enrollmentId 신청 id
     * @return 처리 상태에 대한 응답
     */
    @PatchMapping("/{enrollmentId}/refuse")
    public ResponseEntity<Void> refuseEnrollment(@PathVariable Long enrollmentId) {
        enrollmentService.refuseEnrollment(enrollmentId);
        return ResponseEntity.ok().build();
    }

    /**
     * @param enrollmentId // 신청id
     * @param authentication 사용자 정보
     * @return
     */
    @DeleteMapping("/{enrollmentId}/cancel")
    public ResponseEntity<Void> cancelEnrollment(@PathVariable Long enrollmentId, Authentication authentication){
        enrollmentService.cancelEnrollment(enrollmentId, authentication);
        return ResponseEntity.ok().build();
    }
}
