package com.nta.entity;

import com.nta.enums.PostStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Setter
@Getter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_id",nullable = false)
    String id;
    String description;
    LocalDateTime postTime;

    @OneToOne
    @JoinColumn(name = "product_id")
    Product product;

    @OneToOne
    @JoinColumn(name = "pickup_location")
    Location pickupLocation;
    LocalDateTime pickupTime;

    @OneToOne
    @JoinColumn(name = "delivery_location")
    Location deliveryLocation;
    LocalDateTime deliveryTime;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "shipper_id")
    Shipper shipper;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    Vehicle vehicleType;

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "post")
    Set<PostHistory> history;
}
