package com.nta.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_method_id",nullable = false)
    String id;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "method")
    Set<Payment> payments;

}
