package com.hissah.DTO.Request;

import com.hissah.Entities.User;
import com.hissah.Enums.Role;
import com.hissah.Enums.AccountStatus;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private Role role;
    private AccountStatus accountStatus;

    public User toEntity() {
        User user = new User();
        user.setFullName(this.fullName);
        user.setEmail(this.email);
        user.setPasswordHash(this.passwordHash);
        user.setPhone(this.phone);
        user.setRole(this.role);
        user.setAccountStatus(this.accountStatus);
        return user;
    }
}