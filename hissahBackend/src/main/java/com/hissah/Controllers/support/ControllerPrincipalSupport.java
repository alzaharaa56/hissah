/*
 * Commit message: feat(controller): add authenticated principal validation helpers
 */
package com.hissah.Controllers.support;

import com.hissah.Enums.Role;
import com.hissah.Exceptions.UnauthorizedOperationException;
import com.hissah.Security.CustomUserPrincipal;

public final class ControllerPrincipalSupport {

    private ControllerPrincipalSupport() {
    }

    public static CustomUserPrincipal requirePrincipal(
            CustomUserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedOperationException(
                    "Authentication is required to access this resource."
            );
        }
        return principal;
    }

    public static Long requireUserId(
            CustomUserPrincipal principal
    ) {
        CustomUserPrincipal authenticated =
                requirePrincipal(principal);

        if (authenticated.getId() == null) {
            throw new UnauthorizedOperationException(
                    "The authenticated account has no user ID."
            );
        }
        return authenticated.getId();
    }

    public static Long requireCompanyId(
            CustomUserPrincipal principal
    ) {
        CustomUserPrincipal authenticated =
                requirePrincipal(principal);

        if (authenticated.getCompanyId() == null) {
            throw new UnauthorizedOperationException(
                    "The authenticated account is not linked to a company."
            );
        }
        return authenticated.getCompanyId();
    }

    public static Role requireRole(
            CustomUserPrincipal principal
    ) {
        CustomUserPrincipal authenticated =
                requirePrincipal(principal);

        if (authenticated.getRole() == null) {
            throw new UnauthorizedOperationException(
                    "The authenticated account has no assigned role."
            );
        }
        return authenticated.getRole();
    }
}
