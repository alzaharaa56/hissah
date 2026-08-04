package com.hissah.Repositories;

import com.hissah.Entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByIdAndUserIdAndActiveTrue(Long id, Long userId);
    Page<Notification> findByUserIdAndActiveTrueOrderByCreatedAtDesc(
            Long userId, Pageable pageable);
    List<Notification> findTop10ByUserIdAndActiveTrueOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndReadFlagFalseAndActiveTrue(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notification n set n.readFlag = true, n.readAt = :readAt " +
           "where n.user.id = :userId and n.readFlag = false and n.active = true")
    int markAllAsRead(
            @Param("userId") Long userId,
            @Param("readAt") LocalDateTime readAt);
}
