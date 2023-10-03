package com.foodmate.backend.repository;

import com.foodmate.backend.entity.FoodGroup;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

}
