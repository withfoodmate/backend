package com.foodmate.backend.entity;

import com.foodmate.backend.enums.EnrollmentStatus;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
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
public class FoodGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    private String title;

    private String name;

    @Lob
    private String content;

    @ManyToOne
    private Food food;

    private LocalDateTime groupDateTime;

    private int maximum;

    private int attendance;

    private String storeName;

    private String storeAddress;

    @Column(columnDefinition = "POINT")
    private Point location;

    @CreatedDate
    private LocalDateTime createdDate;

    private LocalDateTime isDeleted;

    public void updateEnrollmentAttendance(int attendance) {
        this.attendance = attendance;
    }

}
