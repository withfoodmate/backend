package com.foodmate.backend.repository;

import com.foodmate.backend.dto.SearchedGroupDto;
import com.foodmate.backend.entity.FoodGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodGroupRepository extends JpaRepository<FoodGroup, Long> {

    // RankingServiceImpl - 많이찾는 식당 랭킹
    @Query("SELECT fg.storeName, fg.storeAddress, COUNT(*) AS count " +
            "FROM FoodGroup fg " +
            "WHERE fg.groupDateTime < CURRENT_TIMESTAMP " +
            "AND fg.isDeleted IS NULL " +
            "GROUP BY fg.storeName, fg.storeAddress " +
            "ORDER BY COUNT(*) DESC")
    List<Object[]> findTop10StoreWithCount(Pageable pageable);

    // GroupServiceImpl - 검색 기능
    @Query("SELECT new com.foodmate.backend.dto.SearchedGroupDto(" +
            "fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate, " +
            "m.id, m.nickname, m.image, f.type, COUNT(e.id)) " +
            "FROM FoodGroup fg " +
            "JOIN Member m ON fg.member.id = m.id " +
            "JOIN Food f ON fg.food.id = f.id " +
            "LEFT JOIN Enrollment e ON fg.id = e.foodGroup.id AND e.status = 'ACCEPT' " +
            "WHERE (fg.title LIKE %:keyword% OR m.nickname LIKE %:keyword%) " +
            "AND fg.groupDateTime BETWEEN :start AND :end " +
            "AND fg.isDeleted IS NULL " +
            "GROUP BY fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate, " +
            "m.id, m.nickname, m.image, f.type " +
            "ORDER BY fg.createdDate DESC")
    Page<SearchedGroupDto> searchByKeyword(String keyword, LocalDateTime start, LocalDateTime end, Pageable pageable);

}
