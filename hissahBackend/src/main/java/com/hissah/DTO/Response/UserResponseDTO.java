package com.hissah.DTO.Response;

import com.hissah.Entities.User;
import com.hissah.Enums.Role;
import com.hissah.Enums.AccountStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;

    public static UserResponseDTO fromEntity(User user) {
        if (user == null) return null;
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setAccountStatus(user.getAccountStatus());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
