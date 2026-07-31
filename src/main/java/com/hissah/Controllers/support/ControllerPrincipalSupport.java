package com.hissah.Controllers.support;

import com.hissah.Entities.Company;
import com.hissah.Enums.Role;
import com.hissah.Exceptions.UnauthorizedOperationException;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerPrincipalSupport {

    private final CompanyRepository companyRepository;

    public CustomUserPrincipal requirePrincipal(CustomUserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedOperationException(
                    "Authentication is required to access this resource."
            );
        }
        return principal;
    }

    public Long requireUserId(
            CustomUserPrincipal principal
    ) {
        Long userId = requirePrincipal(principal).getId();
        if (userId == null) {
            throw new UnauthorizedOperationException(
                    "The authenticated account has no user ID."
            );
        }
        return  userId;
    }

    public Long requireCompanyId(CustomUserPrincipal principal){
        CustomUserPrincipal authenticated = requirePrincipal(principal);

        if (authenticated.getCompanyId() != null){
            return  authenticated.getCompanyId();
        }
        return  companyRepository.findCompanyByUserId(requireUserId(authenticated))
                .map(Company::getId)
                .orElseThrow(() -> new UnauthorizedOperationException(
                        "The authenticated account is not linked to a company."
                ));
    }
    public Role requireRole(CustomUserPrincipal principal){
        Role role = requirePrincipal(principal).getRole();
        if (role == null){
            throw new UnauthorizedOperationException(
                    "The authenticated account has no assigned role."
            );
        }
        return role;
    }


}