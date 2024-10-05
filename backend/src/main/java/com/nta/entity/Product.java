package com.nta.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @Column(name = "product_id",nullable = false)
    String id;
    int quantity;
    String image;
    String mass;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_category_id")
    ProductCategory category;
}
