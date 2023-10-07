package com.foodmate.backend.repository;

import com.foodmate.backend.dto.EnrollmentDto;
import com.foodmate.backend.entity.Enrollment;
import com.foodmate.backend.entity.FoodGroup;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 모임 삭제 후 Status 상태 모임취소로 일괄 변경할 때 사용
    @Modifying
    @Query("UPDATE Enrollment e SET e.status = :status WHERE e.foodGroup.id = :groupId")
    void changeStatusByGroupId(Long groupId, EnrollmentStatus status);

    // 해당 모임에 신청 이력이 존재하는지 확인
    boolean existsByMemberAndFoodGroup(Member member, FoodGroup foodGroup);

    Page<EnrollmentDto.myEnrollmentResponse> findByMemberAndStatusAndFoodGroupGroupDateTimeBetween(
            Member member, EnrollmentStatus status, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);


//    @Query(value = "SELECT NEW com.foodmate.backend.dto.EnrollmentDto(" +
//            "e.id, e.foodGroup.id, m.image, fg.title, fg.name, f.type, " +
//            "fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, e.status) " +
//            "FROM Enrollment e " +
//            "INNER JOIN FoodGroup fg ON fg.id = e.foodGroup.id " +
//            "INNER JOIN Member m ON fg.member.id = m.id " +
//            "INNER JOIN Food f ON fg.food.id = f.id " +
//            "WHERE (e.member.id = :memberId) " +
//            "AND (e.status = :enrollmentStatus) " +
//            "AND (fg.groupDateTime BETWEEN :startDateTime AND :endDateTime)" +
//            "ORDER BY fg.groupDateTime ASC")
//    Page<EnrollmentDto> getMyEnrollment(
//            Long memberId,
//            @Param("enrollmentStatus") EnrollmentStatus enrollmentStatus,
//            LocalDateTime startDateTime,
//            LocalDateTime endDateTime,
//            Pageable pageable);

    // 본인이 생성한 모든 모임의 요청 중 수락한 리스트 조회
    @Query("SELECT e FROM Enrollment e " +
            "JOIN e.foodGroup fg " +
            "WHERE fg.member.id = :id " +
            "AND e.status = 'ACCEPT' " +
            "ORDER BY e.enrollDate ASC")
    Page<Enrollment> findByMyEnrollmentProcessedList(@Param("id") Long readerId, Pageable pageable);

    // 본인이 생성한 모든 모임의 요청 중 신청완료인 리스트 조회
    @Query("SELECT e FROM Enrollment e " +
            "JOIN e.foodGroup fg " +
            "WHERE fg.member.id = :id " +
            "AND e.status = 'SUBMIT' " +
            "ORDER BY e.enrollDate ASC")
    Page<Enrollment> findByMyEnrollmentUnprocessedList(@Param("id") Long readerId, Pageable pageable);

}
