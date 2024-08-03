package com.nta.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "vehicle_id",nullable = false)
    String id;
    String name;
    String description;
    String icon;
    String capacity;

    @OneToMany(mappedBy = "vehicle",fetch = FetchType.LAZY)
    Set<Shipper> shippers;
}
