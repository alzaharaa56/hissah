package com.hissah.DTO.Request;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReadRequestDTO {

    private Long notificationId;

    @Builder.Default
    private Boolean markAll = false;

    @AssertTrue(message = "Provide a notification ID or set markAll to true.")
    public boolean isReadTargetValid() {
        return Boolean.TRUE.equals(markAll) || notificationId != null;
    }
}
