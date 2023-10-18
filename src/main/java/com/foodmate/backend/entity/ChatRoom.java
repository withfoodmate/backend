package com.foodmate.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private FoodGroup foodGroup;

    private int attendance;

    public ChatRoom(FoodGroup foodGroup) {
        this.foodGroup = foodGroup;
    }

}
