package com.foodmate.backend.repository;

import com.foodmate.backend.entity.Enrollment;
import com.foodmate.backend.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 모임의 현재 참여인원 카운트할 때 사용
    int countByFoodGroupIdAndStatus(Long groupId, EnrollmentStatus status);

}
