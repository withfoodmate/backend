package com.foodmate.backend.repository;

import com.foodmate.backend.entity.Likes;
import com.foodmate.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {
    Long countAllByLikedMember(Member likedMember);
}
