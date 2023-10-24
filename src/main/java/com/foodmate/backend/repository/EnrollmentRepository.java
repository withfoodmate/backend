package com.foodmate.backend.repository;

import com.foodmate.backend.dto.EnrollmentDto;
import com.foodmate.backend.entity.Enrollment;
import com.foodmate.backend.entity.FoodGroup;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 모임 삭제 후 Status 상태 모임취소로 일괄 변경할 때 사용
    @Modifying
    @Query("UPDATE Enrollment e SET e.status = :status WHERE e.foodGroup.id = :groupId")
    void updateStatusByGroupId(Long groupId, EnrollmentStatus status);

    // 해당 모임에 신청 이력이 존재하는지 확인
    boolean existsByMemberAndFoodGroup(Member member, FoodGroup foodGroup);

//    Page<EnrollmentDto.myEnrollmentResponse> findByMemberAndStatusAndFoodGroupGroupDateTimeBetween(
//            Member member, EnrollmentStatus submit, EnrollmentStatus cancel, EnrollmentStatus accept, EnrollmentStatus refuse, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);

    Page<EnrollmentDto.myEnrollmentResponse> findByMemberAndStatusInAndFoodGroupGroupDateTimeBetween(
            Member member, List<EnrollmentStatus> statuses, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);

    Page<EnrollmentDto.myEnrollmentResponse> findByMemberAndFoodGroupGroupDateTimeBetween(
            Member member, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);

    // 본인이 생성한 모든 모임의 요청 중 수락한 리스트 조회
    @Query("SELECT e FROM Enrollment e " +
            "JOIN e.foodGroup fg " +
            "WHERE fg.member.id = :id " +
            "AND e.status = :status " +
            "ORDER BY e.enrollDate ASC")
    Page<Enrollment> findByMyEnrollmentProcessedListWithStatus(@Param("id") Long readerId, @Param("status") EnrollmentStatus status, Pageable pageable);


    Page<EnrollmentDto.myReceiveEnrollmentResponse> findByFoodGroupMember(Member member, Pageable pageable);

    // 로그인한 사용자가 참여한 모임 조회
    List<Enrollment> findAllByMemberAndStatusAndFoodGroupGroupDateTimeBetween(
            Member member, EnrollmentStatus status, LocalDateTime start, LocalDateTime end);

    default List<Long> getAcceptedGroupList(Member member, EnrollmentStatus status, LocalDateTime start, LocalDateTime end) {
        return findAllByMemberAndStatusAndFoodGroupGroupDateTimeBetween(member, status, start, end).stream().map(
                Enrollment -> Enrollment.getFoodGroup().getId()).collect(Collectors.toList());
    }

    // WebSocketChannelInterceptor 에서 모임참여자 검증
    boolean existsByMemberIdAndFoodGroupAndStatus(Long memberId, FoodGroup foodGroup, EnrollmentStatus status);

    // 모임 완료 후 Status 가 ACCEPT 였던 신청서의 상태를 GROUP_COMPLETE 로 일괄 변경
    @Modifying
    @Query("UPDATE Enrollment e SET e.status = 'GROUP_COMPLETE' WHERE e.foodGroup = :foodGroup AND e.status = :status")
    void updateStatusToGroupCompleteByFoodGroupAndStatus(FoodGroup foodGroup, EnrollmentStatus status);

}
