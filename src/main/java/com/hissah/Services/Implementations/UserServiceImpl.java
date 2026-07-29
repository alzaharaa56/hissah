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
        return toResponse(getUser(userId));
    }

    @Override
    @Transactional
    public UserResponseDTO updateCurrentUser(
            Long userId,
            UserUpdateRequestDTO request
    ) {
        User user = getUser(userId);

        String fullName = mapper.text(request, "fullName");
        String phone = mapper.text(request, "phone");
        String currentPassword = mapper.text(
                request,
                "currentPassword",
                "oldPassword"
        );
        String newPassword = mapper.text(
                request,
                "newPassword",
                "password"
        );
