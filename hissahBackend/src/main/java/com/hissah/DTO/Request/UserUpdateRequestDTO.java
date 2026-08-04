package com.hissah.DTO.Request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequestDTO {

    @Size(max = 120, message = "Full name must not exceed 120 characters.")
    private String fullName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters.")
    private String phone;

    private String currentPassword;

    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters.")
    private String newPassword;

    @AssertTrue(message = "Current password is required when setting a new password.")
    public boolean isPasswordChangeValid() {
        return newPassword == null
                || newPassword.isBlank()
                || (currentPassword != null && !currentPassword.isBlank());
    }
}
