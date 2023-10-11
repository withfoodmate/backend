package com.foodmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMember {
    @Id
    private Long id;

    @ManyToOne
    private FoodGroup foodGroup;
}
