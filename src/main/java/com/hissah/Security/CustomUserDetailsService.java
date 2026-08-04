package com.hissah.Security;

import com.hissah.Entities.Company;
import com.hissah.Entities.User;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: "
                                        + email
                        )
                );

        Long companyId = companyRepository
                .findByUserId(user.getId())
                .map(Company::getId)
                .orElse(null);

        return new CustomUserPrincipal(
                user,
                companyId
        );
    }
}