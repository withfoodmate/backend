package com.foodmate.backend.repository;

import com.foodmate.backend.entity.Comment;
import com.foodmate.backend.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    // 해당 댓글의 대댓글 일괄 삭제
    void deleteAllByComment(Comment comment);

}
