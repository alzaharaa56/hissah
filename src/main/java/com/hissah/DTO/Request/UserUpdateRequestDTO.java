package com.hissah.DTO.Request;

import com.hissah.Entities.User;
import com.hissah.Enums.Role;
import com.hissah.Enums.AccountStatus;
import lombok.Data;

@Data
public class UserUpdateRequestDTO {
    private String fullName;
    private String phone;
    private Role role;
    private AccountStatus accountStatus;

    public void updateEntity(User user) {
        if (user == null) return;
        if (this.fullName != null) user.setFullName(this.fullName);
        if (this.phone != null) user.setPhone(this.phone);
        if (this.role != null) user.setRole(this.role);
        if (this.accountStatus != null) user.setAccountStatus(this.accountStatus);
    }
}