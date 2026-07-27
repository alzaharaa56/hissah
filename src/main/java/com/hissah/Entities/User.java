package com.hissah.Entities;

import com.hissah.Enums.AccountStatus;
import com.hissah.Enums.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    @Column(unique = true, nullable = false)
    private String email;
    private String passwordHash;
    private String phone;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}