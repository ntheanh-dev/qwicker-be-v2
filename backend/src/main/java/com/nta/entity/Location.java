package com.nta.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Setter
@Getter
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "location_id",nullable = false)
    String id;
    String name; //for user or company
    String phoneNumber;
    String address;
    BigDecimal latitude;
    BigDecimal longitude;
}
