package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.AdminBootstrapRequestDTO;
import com.hissah.Entities.User;
import com.hissah.Enums.AccountStatus;
import com.hissah.Enums.Role;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.DuplicateResourceException;
import com.hissah.Repositories.UserRepository;
import com.hissah.Services.AdminBootstrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminBootstrapServiceImpl
        implements AdminBootstrapService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-bootstrap.enabled:false}")
    private boolean bootstrapEnabled;

    @Value("${app.admin-bootstrap.key}")
    private String expectedSetupKey;

    @Override
    @Transactional
    public Map<String, Object> createFirstAdmin(
            AdminBootstrapRequestDTO request,
            String suppliedSetupKey
    ) {
        if (!bootstrapEnabled) {
            throw new BusinessRuleException(
                    "Administrator bootstrap is disabled."
            );
        }

        if (suppliedSetupKey == null
                || !suppliedSetupKey.equals(expectedSetupKey)) {
            throw new BusinessRuleException(
                    "Invalid administrator setup key."
            );
        }

        boolean adminAlreadyExists = userRepository.findAll()
                .stream()
                .anyMatch(user -> user.getRole() == Role.ADMIN);

        if (adminAlreadyExists) {
            throw new DuplicateResourceException(
                    "An administrator account already exists."
            );
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.findUserByEmail(email).isPresent()) {
            throw new DuplicateResourceException(
                    "An account already exists with this email."
            );
        }

        User admin = new User();
        admin.setFullName(request.getFullName().trim());
        admin.setEmail(email);
        admin.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        admin.setPhone(request.getPhone().trim());
        admin.setRole(Role.ADMIN);
        admin.setAccountStatus(AccountStatus.ACTIVE);
        admin.setActive(true);

        User saved = userRepository.save(admin);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", saved.getId());
        response.put("fullName", saved.getFullName());
        response.put("email", saved.getEmail());
        response.put("role", saved.getRole());
        response.put("accountStatus", saved.getAccountStatus());
        response.put(
                "message",
                "Administrator account created successfully."
        );

        return response;
    }
}
