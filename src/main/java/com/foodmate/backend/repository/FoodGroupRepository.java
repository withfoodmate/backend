package com.foodmate.backend.repository;

import com.foodmate.backend.dto.SearchedGroupDto;
import com.foodmate.backend.entity.FoodGroup;
import org.locationtech.jts.geom.Point;
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

    // GroupServiceImpl - 오늘 모임 조회
    @Query("SELECT new com.foodmate.backend.dto.SearchedGroupDto(" +
            "fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate, " +
            "m.id, m.nickname, m.image, f.type, COUNT(e.id)) " +
            "FROM FoodGroup fg " +
            "JOIN Member m ON fg.member.id = m.id " +
            "JOIN Food f ON fg.food.id = f.id " +
            "LEFT JOIN Enrollment e ON fg.id = e.foodGroup.id AND e.status = 'ACCEPT' " +
            "WHERE fg.groupDateTime BETWEEN :start AND :end " +
            "AND fg.isDeleted IS NULL " +
            "GROUP BY fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate, " +
            "m.id, m.nickname, m.image, f.type " +
            "ORDER BY fg.groupDateTime ASC")
    Page<SearchedGroupDto> getTodayGroupList(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // GroupServiceImpl - 전체 모임 조회
    @Query("SELECT new com.foodmate.backend.dto.SearchedGroupDto(" +
            "fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate, " +
            "m.id, m.nickname, m.image, f.type, COUNT(e.id)) " +
            "FROM FoodGroup fg " +
            "JOIN Member m ON fg.member.id = m.id " +
            "JOIN Food f ON fg.food.id = f.id " +
            "LEFT JOIN Enrollment e ON fg.id = e.foodGroup.id AND e.status = 'ACCEPT' " +
            "WHERE fg.groupDateTime BETWEEN :start AND :end " +
            "AND fg.isDeleted IS NULL " +
            "GROUP BY fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate, " +
            "m.id, m.nickname, m.image, f.type " +
            "ORDER BY fg.createdDate DESC")
    Page<SearchedGroupDto> getAllGroupList(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // TODO 5km 이내 필터링 조건 다시 알아보고 수정할 것
    // GroupServiceImpl - 거리순 조회 & 내 근처 모임
    @Query("SELECT new com.foodmate.backend.dto.SearchedGroupDto(" +
            "fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate, " +
            "m.id, m.nickname, m.image, f.type, COUNT(e.id)) " +
            "FROM FoodGroup fg " +
            "JOIN Member m ON fg.member.id = m.id " +
            "JOIN Food f ON fg.food.id = f.id " +
            "LEFT JOIN Enrollment e ON fg.id = e.foodGroup.id AND e.status = 'ACCEPT' " +
            "WHERE fg.groupDateTime BETWEEN :start AND :end " +
            "AND fg.isDeleted IS NULL " +
            "AND FUNCTION('ST_DISTANCE', fg.location, :userLocation) < 5000 " +
            "GROUP BY fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate, " +
            "m.id, m.nickname, m.image, f.type " +
            "ORDER BY FUNCTION('ST_DISTANCE', fg.location, :userLocation)")
    Page<SearchedGroupDto> searchByLocation(Point userLocation, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // GroupServiceImpl - 날짜별 조회
    @Query("SELECT new com.foodmate.backend.dto.SearchedGroupDto(" +
            "fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate, " +
            "m.id, m.nickname, m.image, f.type, COUNT(e.id)) " +
            "FROM FoodGroup fg " +
            "JOIN Member m ON fg.member.id = m.id " +
            "JOIN Food f ON fg.food.id = f.id " +
            "LEFT JOIN Enrollment e ON fg.id = e.foodGroup.id AND e.status = 'ACCEPT' " +
            "WHERE fg.groupDateTime BETWEEN :start AND :end " +
            "AND fg.isDeleted IS NULL " +
            "GROUP BY fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate, " +
            "m.id, m.nickname, m.image, f.type " +
            "ORDER BY fg.groupDateTime ASC")
    Page<SearchedGroupDto> searchByDate(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // GroupServiceImpl - 메뉴별 조회
    @Query("SELECT new com.foodmate.backend.dto.SearchedGroupDto(" +
            "fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate, " +
            "m.id, m.nickname, m.image, f.type, COUNT(e.id)) " +
            "FROM FoodGroup fg " +
            "JOIN Member m ON fg.member.id = m.id " +
            "JOIN Food f ON fg.food.id = f.id " +
            "LEFT JOIN Enrollment e ON fg.id = e.foodGroup.id AND e.status = 'ACCEPT' " +
            "WHERE f.type IN :foodTypes " +
            "AND fg.groupDateTime BETWEEN :start AND :end " +
            "AND fg.isDeleted IS NULL " +
            "GROUP BY fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate, " +
            "m.id, m.nickname, m.image, f.type " +
            "ORDER BY fg.createdDate DESC")
    Page<SearchedGroupDto> searchByFood(List<String> foodTypes, LocalDateTime start, LocalDateTime end, Pageable pageable);

}
