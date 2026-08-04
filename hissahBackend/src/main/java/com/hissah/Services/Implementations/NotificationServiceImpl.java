package com.hissah.Services.Implementations;

import com.hissah.DTO.Response.NotificationResponseDTO;
import com.hissah.Entities.Notification;
import com.hissah.Entities.User;
import com.hissah.Enums.NotificationType;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.NotificationRepository;
import com.hissah.Repositories.UserRepository;
import com.hissah.Services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public NotificationResponseDTO create(
            Long userId,
            String title,
            String message,
            NotificationType type
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(requireText(title, "Notification title"));
        notification.setMessage(requireText(message, "Notification message"));
        notification.setType(
                type == null ? NotificationType.SYSTEM : type
        );
        notification.setReadFlag(false);

        return toResponse(notificationRepository.save(notification));
    }

    @Override
    public Page<NotificationResponseDTO> getForUser(
            Long userId,
            Pageable pageable
    ) {
        return notificationRepository
                .findByUserIdAndActiveTrueOrderByCreatedAtDesc(
                        userId,
                        pageable
                )
                .map(this::toResponse);
    }

    @Override
    public List<NotificationResponseDTO> getRecent(
            Long userId
    ) {
        return notificationRepository
                .findTop10ByUserIdAndActiveTrueOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(
            Long notificationId,
            Long userId
    ) {
        Notification notification =
                notificationRepository
                        .findByIdAndUserIdAndActiveTrue(
                                notificationId,
                                userId
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Notification not found for the current user."
                        ));

        if (!Boolean.TRUE.equals(
                notification.getReadFlag())) {
            notification.markAsRead();
            notification =
                    notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(
                userId,
                LocalDateTime.now()
        );
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepository
                .countByUserIdAndReadFlagFalseAndActiveTrue(
                        userId
                );
    }

    private NotificationResponseDTO toResponse(
            Notification notification
    ) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(Boolean.TRUE.equals(
                        notification.getReadFlag()
                ))
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }

    private String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }
        return value.trim();
    }
}
