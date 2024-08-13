package com.nta.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id",nullable = false)
    String id;
    BigDecimal price;
    LocalDateTime paidAt;
    boolean isPosterPay;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    PaymentMethod method;
}




