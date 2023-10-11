package com.foodmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    private Long id;

    @ManyToOne
    private ChatMember chatMember;

    @ManyToOne
    private Member member;

    private String content;

    private LocalDateTime createDate;
}
