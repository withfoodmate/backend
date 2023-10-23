package com.foodmate.backend.entity;

import com.foodmate.backend.enums.EnrollmentStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private FoodGroup foodGroup;

    @CreatedDate
    private LocalDateTime enrollDate;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    @LastModifiedDate
    private LocalDateTime decisionDate;

    public void updateEnrollmentStatus(EnrollmentStatus status) {
        this.status = status;
    }

}
