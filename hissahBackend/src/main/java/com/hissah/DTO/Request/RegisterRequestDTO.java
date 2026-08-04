/*
 * Commit message: fix(dto): combine user and company registration fields safely
 */
package com.hissah.DTO.Request;

import com.hissah.Enums.CompanyType;
import com.hissah.Enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "Full name is required.")
    @Size(max = 120)
    private String fullName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 160)
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank(message = "Phone is required.")
    @Size(max = 20)
    private String phone;

    @NotNull(message = "Account role is required.")
    private Role role;

    @NotBlank(message = "Company legal name is required.")
    @Size(max = 180)
    private String legalName;

    @Size(max = 180)
    private String tradingName;

    @NotNull(message = "Company type is required.")
    private CompanyType companyType;

    @NotBlank(message = "Commercial registration number is required.")
    @Size(max = 80)
    private String crNumber;

    @NotBlank(message = "Governorate is required.")
    @Size(max = 100)
    private String governorate;

    @Size(max = 5000)
    private String description;

    @Builder.Default
    private List<Long> categoryIds = new ArrayList<>();
}
