package com.hissah.Services;

import com.hissah.DTO.Request.UserUpdateRequestDTO;
import com.hissah.DTO.Response.UserResponseDTO;

public interface UserService {
    UserResponseDTO getCurrentUser(Long userId);
    UserResponseDTO updateCurrentUser(
            Long userId,
            UserUpdateRequestDTO request
    );
}