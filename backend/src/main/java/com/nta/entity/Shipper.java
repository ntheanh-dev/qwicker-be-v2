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
    @Column(name = "identity_f",length = 120)
    String identityF;
    @Column(name = "identity_b",length = 120)
    String identityB;
    @Column(length = 15)
    String vehicleNumber;
    @OneToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    Vehicle vehicle;
}
