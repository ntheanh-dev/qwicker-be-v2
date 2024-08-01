package com.nta.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String adminId;

    @OneToOne
    @JoinColumn(referencedColumnName = "userId")
    private User user;
}
