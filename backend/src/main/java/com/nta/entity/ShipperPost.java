package com.nta.entity;

import com.nta.enums.ShipperPostStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "shipper_post")
public class ShipperPost {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "shipper_post_id",nullable = false)
    String id;

    @ManyToOne
    @MapsId("id")
    @JoinColumn(name = "id", nullable = false)
    private Shipper shipper;

    @ManyToOne
    @MapsId("id")
    @JoinColumn(name = "id",nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    private ShipperPostStatus status;
    private LocalDateTime joinedAt;
}
