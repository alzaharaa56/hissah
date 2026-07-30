package com.hissah.Controllers;

import com.hissah.DTO.Request.UserUpdateRequestDTO;
import com.hissah.DTO.Response.UserResponseDTO;
import com.hissah.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long userId
    ) {
        Long currentUserId = userId != null ? userId : 1L;

        UserResponseDTO response = userService.getCurrentUser(currentUserId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @RequestParam(required = false) Long userId,
            @RequestBody UserUpdateRequestDTO request
    ) {
        Long currentUserId = userId != null ? userId : 1L;

        UserResponseDTO response = userService.updateCurrentUser(currentUserId, request);
        return ResponseEntity.ok(response);
    }
}
