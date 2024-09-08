package com.nta.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rating_id",nullable = false)
    String id;
    double rating;
    String feedback;
    LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    Shipper shipper;

    @JsonBackReference
    @JoinColumn(name = "post_id")
    @OneToOne(fetch = FetchType.LAZY)
    Post post;
}
