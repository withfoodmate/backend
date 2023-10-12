package com.foodmate.backend.repository;

import com.foodmate.backend.entity.ChatMember;
import com.foodmate.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    List<ChatMember> findByMember(Member member);
}
