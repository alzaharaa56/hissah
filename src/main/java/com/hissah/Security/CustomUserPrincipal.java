package com.hissah.Security;

import com.hissah.Entities.User;
import com.hissah.Enums.AccountStatus;
import com.hissah.Enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final String passwordHash;
    private final Long companyId;
    private final Role role;
    private final boolean active;
    private final AccountStatus accountStatus;

    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserPrincipal(
            User user,
            Long companyId
    ) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.companyId = companyId;
        this.role = user.getRole();
        this.active = Boolean.TRUE.equals(user.getActive());
        this.accountStatus = user.getAccountStatus();

        String roleName = user.getRole() == null
                ? "USER"
                : user.getRole().name();

        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority(
                        "ROLE_" + roleName
                )
        );
    }

    public Long getUserId() {
        return userId;
    }

    public Long getId() {
        return userId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountStatus != AccountStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active
                && accountStatus == AccountStatus.ACTIVE;
    }
}