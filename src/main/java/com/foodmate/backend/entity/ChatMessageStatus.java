package com.foodmate.backend.entity;

import com.foodmate.backend.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private ChatMessage chatMessage;

    @Enumerated(EnumType.STRING)
    private MessageStatus messageStatus;

}
