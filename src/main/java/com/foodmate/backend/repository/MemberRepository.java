package com.foodmate.backend.repository;

import com.foodmate.backend.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // RankingService - 좋아요 랭킹
    List<Member> findTop10ByOrderByLikesDesc();

    // 이메일을 통해 회원 찾기
    Optional<Member> findByEmail(String email);

    // 닉네임을 통해 회원 찾기
    Optional<Member> findByNickname(String nickname);

    // RefreshToken 정보 찾기
    Optional<Member> findByRefreshToken(String refreshToken);

    // RankingService - 모임왕 랭킹
    @Query("SELECT m.id, m.nickname, m.image, COUNT(fg.member) AS count " +
            "FROM Member m " +
            "JOIN FoodGroup fg ON m.id = fg.member.id " +
            "WHERE fg.groupDateTime < :comparisonDate " +
            "AND fg.isDeleted IS NULL " +
            "GROUP BY m.id, m.nickname, m.image " +
            "ORDER BY COUNT(fg.member) DESC")
    List<Object[]> findTop10MemberWithCount(LocalDateTime comparisonDate, Pageable pageable);

    Optional<Member> findByEmailAuthKey(String emailAuthKey);

}
