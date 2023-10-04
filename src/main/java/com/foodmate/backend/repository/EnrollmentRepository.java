package com.foodmate.backend.repository;

import com.foodmate.backend.entity.Enrollment;
import com.foodmate.backend.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 모임의 현재 참여인원 카운트할 때 사용
    int countByFoodGroupIdAndStatus(Long groupId, EnrollmentStatus status);

    // 모임 삭제 후 Status 상태 모임취소로 일괄 변경할 때 사용
    @Modifying
    @Query("UPDATE Enrollment e SET e.status = :status WHERE e.foodGroup.id = :groupId")
    void changeStatusByGroupId(Long groupId, EnrollmentStatus status);

}
