package com.foodmate.backend.repository;

import com.foodmate.backend.entity.Food;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    // RankingService - 음식 카테고리 랭킹
    @Query("SELECT f.type, f.image, COUNT(fg.food) AS count " +
            "FROM Food f " +
            "JOIN FoodGroup fg ON f.id = fg.food.id " +
            "WHERE fg.groupDateTime < :comparisonDate " +
            "AND fg.isDeleted IS NULL " +
            "GROUP BY f.type, f.image " +
            "ORDER BY COUNT(fg.food) DESC")
    List<Object[]> findTop10FoodWithCount(LocalDateTime comparisonDate, Pageable pageable);

    // 음식 이름으로 푸드 엔티티 찾기
    Optional<Food> findByType(String foodName);

    // 입력받은 음식 이름이 DB 에 존재하는지 확인
    boolean existsByType(String foodName);

}
