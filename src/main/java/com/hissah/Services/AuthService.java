package com.hissah.Services;



import com.hissah.DTO.Request.LoginRequestDTO;
import com.hissah.DTO.Request.RegisterRequestDTO;
import com.hissah.DTO.Response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
}