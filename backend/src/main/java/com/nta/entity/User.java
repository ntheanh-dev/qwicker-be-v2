package com.nta.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    String userId;
    @Column(nullable = false,length = 50)
    String username;
    @Column(nullable = false)
    String password;
    String firstName;
    String lastName;
    String email;
    String avatar;
}
