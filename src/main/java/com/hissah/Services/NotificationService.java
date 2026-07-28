package com.hissah.Services;

import com.hissah.DTO.Responses.NotificationResponseDTO;
import com.hissah.Enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    NotificationResponseDTO create(
            Long userId,
            String title,
            String message,
            NotificationType type);

    Page<NotificationResponseDTO> getForUser(
            Long userId, Pageable pageable);

    List<NotificationResponseDTO> getRecent(Long userId);

    NotificationResponseDTO markAsRead(
            Long notificationId, Long userId);

    int markAllAsRead(Long userId);

    long countUnread(Long userId);
}
