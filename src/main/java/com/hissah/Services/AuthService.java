package com.hissah.Services;

import com.hissah.DTO.Request.LoginRequestDTO;
import com.hissah.DTO.Request.RegisterRequestDTO;
import com.hissah.DTO.Response.AuthResponseDTO;
import com.hissah.Entities.User;
import com.hissah.Enums.Role;
import com.hissah.Exceptions.DuplicateResourceException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Repositories.UserRepository;
import com.hissah.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email is already in use: " + request.getEmail());
        }

        User user = request.toEntity();
        user.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));


        if (user.getRole() == null) {
            user.setRole(Role.SUBCONTRACTOR);
        }

        User savedUser = userRepository.save(user);

        String jwtToken = jwtService.generateToken(savedUser);

        return new AuthResponseDTO(
                jwtToken,
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    public AuthResponseDTO authenticate(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPasswordHash()
                )
        );

        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String jwtToken = jwtService.generateToken(user.getEmail());

        return new AuthResponseDTO(
                jwtToken,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}