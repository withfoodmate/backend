package com.foodmate.backend.repository;

import com.foodmate.backend.entity.FoodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<FoodGroup, Long> {
}
