package com.foodmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Preference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    Member member;

    @ManyToOne
    Food food;

    public void updateFood(Food newFood) {
        this.food = newFood;
    }

    public void updateMember(Member member) {
        this.member = member;
    }
}
