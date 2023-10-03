package com.foodmate.backend.repository;

import com.foodmate.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // RankingServiceImpl - 좋아요 랭킹
    List<Member> findTop10ByOrderByLikesDesc();

}
