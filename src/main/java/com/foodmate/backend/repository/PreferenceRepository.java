package com.foodmate.backend.repository;

import com.foodmate.backend.entity.Member;
import com.foodmate.backend.entity.Preference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreferenceRepository extends JpaRepository<Preference,Long> {

    List<Preference> findAllByMember(Member member);

    void deleteByMember(Member member);
}
