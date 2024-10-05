package com.nta.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nta.enums.PostStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_id",nullable = false)
    String id;
    String description;
    LocalDateTime postTime;
    String requestType; // now or latter
    @Enumerated(EnumType.STRING)
    PostStatus status;

    @OneToOne
    @JoinColumn(name = "product_id")
    Product product;

    @OneToOne
    @JoinColumn(name = "pickup_location")
    Location pickupLocation;
    LocalDateTime pickupDatetime;

    @OneToOne
    @JoinColumn(name = "drop_location")
    Location dropLocation;
    LocalDateTime dropDateTime;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name="vehicle_id")
    Vehicle vehicleType;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "post")
    Set<PostHistory> history;

    @OneToOne
    @JoinColumn(name = "payment_id")
    Payment payment;
}
