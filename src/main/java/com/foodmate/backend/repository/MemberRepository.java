package com.foodmate.backend.repository;

import com.foodmate.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // RankingServiceImpl - 좋아요 랭킹
    List<Member> findTop10ByOrderByLikesDesc();

    // 이메일을 통해 회원 찾기
    Optional<Member> findByEmail(String email);

}
