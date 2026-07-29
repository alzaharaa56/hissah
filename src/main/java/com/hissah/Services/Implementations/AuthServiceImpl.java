package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.LoginRequestDTO;
import com.hissah.DTO.Request.RegisterRequestDTO;
import com.hissah.DTO.Response.AuthResponseDTO;
import com.hissah.Entities.User;
import com.hissah.Enums.AccountStatus;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;


    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setAccountStatus(AccountStatus.PENDING);

        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser);
    }

    public AuthResponseDTO authenticate(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPasswordHash()
                )
        );

        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        return buildAuthResponse(user);
    }

    public AuthResponseDTO buildAuthResponse(User user) {
        String token = "DUMMY_JWT_TOKEN";
        return new AuthResponseDTO(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}