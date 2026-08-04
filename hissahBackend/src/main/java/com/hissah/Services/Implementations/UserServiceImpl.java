package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.UserUpdateRequestDTO;
import com.hissah.DTO.Response.UserResponseDTO;
import com.hissah.Entities.User;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.UserRepository;
import com.hissah.Services.UserService;
import com.hissah.Services.Implementations.Support.ServiceDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServiceDtoMapper mapper;

    @Override
    public UserResponseDTO getCurrentUser(Long userId) {
        User user = findUserOrThrow(userId);
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateCurrentUser(
            Long userId,
            UserUpdateRequestDTO request
    ) {
        User user = findUserOrThrow(userId);

        String fullName = mapper.text(request, "fullName");
        String phone = mapper.text(request, "phone");

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName.trim());
        }
        if (phone != null && !phone.isBlank()) {
            user.setPhone(phone.trim());
        }

        String currentPassword = mapper.text(request, "currentPassword", "oldPassword");
        String newPassword = mapper.text(request, "newPassword", "password");

        if (newPassword != null && !newPassword.isBlank()) {
            if (currentPassword == null || currentPassword.isBlank()) {
                throw new BusinessRuleException("Current password is required to set a new password.");
            }
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                throw new BusinessRuleException("Incorrect current password.");
            }
            user.setPasswordHash(passwordEncoder.encode(newPassword));
        }

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private UserResponseDTO toResponse(User user) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", user.getId());
        values.put("fullName", user.getFullName());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("role", user.getRole());
        values.put("accountStatus", user.getAccountStatus());
        values.put("createdAt", user.getCreatedAt());
        return mapper.toDto(values, UserResponseDTO.class);
    }
}