package com.nta.entity;

import com.nta.enums.PostStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class PostHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_history_id",nullable = false)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    Post post;
    LocalDateTime statusChangeDate;
    String reason;

    @Enumerated(EnumType.STRING)
    PostStatus status;
}
