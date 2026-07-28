package com.hissah.DTO.Request;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String passwordHash;
}
