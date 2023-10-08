package com.foodmate.backend.repository;

import com.foodmate.backend.entity.Comment;
import com.foodmate.backend.entity.FoodGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 해당 모임의 댓글 전체 조회
    Page<Comment> findAllByFoodGroup(FoodGroup foodGroup, Pageable pageable);

}
