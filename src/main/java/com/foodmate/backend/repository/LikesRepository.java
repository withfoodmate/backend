package com.foodmate.backend.repository;

import com.foodmate.backend.entity.Likes;
import com.foodmate.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {
    Long countAllByLiked(Member liked);

    Optional<Likes> findByLikedAndLiker(Member liked, Member liker);
}
