package com.foodmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Likes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member liked;

    @ManyToOne
    private Member liker;

    public static Likes makeLikes(Member liked, Member liker){
        return Likes.builder()
                .liked(liked)
                .liker(liker)
                .build();
    }
}
