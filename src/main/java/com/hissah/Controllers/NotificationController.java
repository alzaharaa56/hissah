package com.hissah.Controllers;

import com.hissah.Controllers.support.ControllerPrincipalSupport;
import com.hissah.DTO.Response.NotificationResponseDTO;
import com.hissah.Security.CustomUserPrincipal;
import com.hissah.Services.NotificationService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
@PreAuthorize(
        "hasAnyRole('ADMIN','MAIN_CONTRACTOR','SUBCONTRACTOR')"
)
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getMyNotifications(
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                notificationService.getForUser(
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        ),
                        pageable
                )
        );
    }

    @GetMapping("/recent")
    public ResponseEntity<List<NotificationResponseDTO>> getRecent(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                notificationService.getRecent(
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        )
                )
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        long unreadCount =
                notificationService.countUnread(
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        )
                );

        return ResponseEntity.ok(
                Map.of("unreadCount", unreadCount)
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable @Positive Long notificationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                notificationService.markAsRead(
                        notificationId,
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        )
                )
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        int updatedCount =
                notificationService.markAllAsRead(
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        )
                );

        return ResponseEntity.ok(
                Map.of("updatedCount", updatedCount)
        );
    }
}
