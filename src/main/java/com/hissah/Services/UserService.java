package com.hissah.Services;

import com.hissah.DTO.Request.UserUpdateRequestDTO;
import com.hissah.DTO.Response.UserResponseDTO;
import com.hissah.Entities.User;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserResponseDTO getCurrentUserProfile(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return UserResponseDTO.fromEntity(user);
    }


    @Transactional
    public UserResponseDTO updateProfile(String email, UserUpdateRequestDTO request) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));


        request.updateEntity(user);

        User updatedUser = userRepository.save(user);

        return UserResponseDTO.fromEntity(updatedUser);
    }
}