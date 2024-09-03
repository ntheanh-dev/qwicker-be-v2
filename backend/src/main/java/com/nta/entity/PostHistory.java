package com.nta.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.nta.enums.PostStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_history_id",nullable = false)
    String id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    Post post;
    LocalDateTime statusChangeDate;
    String description;
    String photo;

    @Enumerated(EnumType.STRING)
    PostStatus status;
}
