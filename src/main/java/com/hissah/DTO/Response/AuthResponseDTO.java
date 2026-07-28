package com.hissah.DTO.Response;
import com.hissah.Enums.Role;
import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String fullName;

    private String email;
    private Role role;

    public AuthResponseDTO(String token, Long id, String fullName, String email, Role role) {
        this.token = token;
        this.id = id;
        this.fullName = fullName;

        this.email = email;
        this.role = role;
    }
}
