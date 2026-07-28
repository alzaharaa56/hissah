package com.hissah.Entities;

import com.hissah.Enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
// @NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user", columnList = "user_id"),
        @Index(name = "idx_notification_unread", columnList = "user_id, read_flag")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private NotificationType type = NotificationType.SYSTEM;

    @Column(name = "read_flag", nullable = false)
    private Boolean readFlag = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    public Notification() {
    }

    @PrePersist
    void beforeInsert() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (type == null) type = NotificationType.SYSTEM;
        if (readFlag == null) readFlag = false;
        if (active == null) active = true;
    }

    public void markAsRead() {
        this.readFlag = true;
        this.readAt = LocalDateTime.now();
    }
}
