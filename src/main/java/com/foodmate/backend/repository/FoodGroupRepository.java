package com.foodmate.backend.repository;

import com.foodmate.backend.dto.NearbyGroupDto;
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

    // RankingService - 많이찾는 식당 랭킹
    @Query("SELECT fg.storeName, fg.storeAddress, COUNT(*) AS count " +
            "FROM FoodGroup fg " +
            "WHERE fg.groupDateTime < :comparisonDate " +
            "AND fg.isDeleted IS NULL " +
            "GROUP BY fg.storeName, fg.storeAddress " +
            "ORDER BY COUNT(*) DESC")
    List<Object[]> findTop10StoreWithCount(LocalDateTime comparisonDate, Pageable pageable);

    // GroupService - 검색 기능
    @Query("SELECT new com.foodmate.backend.dto.SearchedGroupDto(fg) " +
            "FROM FoodGroup fg " +
            "JOIN Member m ON fg.member.id = m.id " +
            "WHERE (fg.title LIKE %:keyword% OR m.nickname LIKE %:keyword%) " +
            "AND fg.groupDateTime BETWEEN :start AND :end " +
            "AND fg.isDeleted IS NULL " +
            "ORDER BY fg.createdDate DESC")
    Page<SearchedGroupDto> searchByKeyword(String keyword, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // GroupService - 오늘 모임 조회 &  날짜별 조회
    Page<FoodGroup> findAllByGroupDateTimeBetweenAndIsDeletedIsNullOrderByGroupDateTimeAsc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    default Page<SearchedGroupDto> searchByDate(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return findAllByGroupDateTimeBetweenAndIsDeletedIsNullOrderByGroupDateTimeAsc(
                start, end, pageable).map(foodGroup -> new SearchedGroupDto(foodGroup));
    }

    // GroupService - 전체 모임 조회
    Page<FoodGroup> findAllByGroupDateTimeBetweenAndIsDeletedIsNullOrderByCreatedDateDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    default Page<SearchedGroupDto> getAllGroupList(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return findAllByGroupDateTimeBetweenAndIsDeletedIsNullOrderByCreatedDateDesc(
                start, end, pageable).map(foodGroup -> new SearchedGroupDto(foodGroup));
    }

    // GroupService - 거리순 조회
    @Query("SELECT new com.foodmate.backend.dto.SearchedGroupDto(fg) " +
            "FROM FoodGroup fg " +
            "WHERE fg.groupDateTime BETWEEN :start AND :end " +
            "AND fg.isDeleted IS NULL " +
            "ORDER BY FUNCTION('ST_Distance_Sphere', fg.location, :userLocation)")
    Page<SearchedGroupDto> searchByLocation(Point userLocation, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // GroupService - 메뉴별 조회
    @Query("SELECT new com.foodmate.backend.dto.SearchedGroupDto(fg) " +
            "FROM FoodGroup fg " +
            "JOIN Food f ON fg.food.id = f.id " +
            "WHERE f.type IN :foodTypes " +
            "AND fg.groupDateTime BETWEEN :start AND :end " +
            "AND fg.isDeleted IS NULL " +
            "ORDER BY fg.createdDate DESC")
    Page<SearchedGroupDto> searchByFood(List<String> foodTypes, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // GroupService - 내 근처 모임
    @Query("SELECT new com.foodmate.backend.dto.NearbyGroupDto(fg) " +
            "FROM FoodGroup fg " +
            "WHERE fg.groupDateTime BETWEEN :start AND :end " +
            "AND fg.isDeleted IS NULL " +
            "AND FUNCTION('ST_Distance_Sphere', fg.location, :userLocation) < 5000 " +
            "ORDER BY FUNCTION('ST_Distance_Sphere', fg.location, :userLocation)")
    Page<NearbyGroupDto> getNearbyGroupList(Point userLocation, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<FoodGroup> findAllByGroupDateTimeBetween(LocalDateTime start, LocalDateTime end);

}
