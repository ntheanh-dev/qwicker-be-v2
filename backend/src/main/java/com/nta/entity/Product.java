package com.nta.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id",nullable = false)
    String id;
    int quantity;
    String image;
    String mass;

    @ManyToOne
    @JoinColumn(name = "product_category_id")
    ProductCategory category;
}
