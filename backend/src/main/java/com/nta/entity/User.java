package com.nta.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id",nullable = false)
    String id;
    @Column(nullable = false,length = 50,unique = true)
    String username;
    @Column(nullable = false)
    String password;
    String firstName;
    String lastName;
    @Column(nullable = false,length = 30,unique = true)
    String email;
    String avatar;

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY)
    Set<Post> posts;

    @ManyToMany
    Set<Role> roles;
}
