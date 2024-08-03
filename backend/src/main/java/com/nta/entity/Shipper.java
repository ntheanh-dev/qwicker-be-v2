package com.nta.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Shipper {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "shipper_id",nullable = false)
    String id;
    String identity_f;
    String identity_b;
    String vehicle_number;

    @OneToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    Vehicle vehicle;
}
