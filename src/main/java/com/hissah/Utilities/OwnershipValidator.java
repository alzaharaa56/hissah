package com.hissah.Utils;

import com.hissah.Entities.Bid;
import com.hissah.Entities.Project;
import com.hissah.Entities.WorkPackage;
import com.hissah.Exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Component;

/**
 * Central ownership checks used by services.
 * Controllers must not rely only on hidden buttons or browser-supplied company IDs.
 */
@Component
public class OwnershipValidator {

    public void validateCompanyOwnership(Long currentCompanyId, Long resourceCompanyId) {
        requireId(currentCompanyId, "The authenticated user is not linked to a company.");
        requireId(resourceCompanyId, "The requested resource has no company owner.");
        if (!currentCompanyId.equals(resourceCompanyId)) {
            throw new UnauthorizedOperationException("You do not own this company resource.");
        }
    }

    public void validateProjectOwnership(Long currentCompanyId, Project project) {
        if (project == null || project.getContractorCompany() == null) {
            throw new UnauthorizedOperationException("The project owner could not be identified.");
        }
        validateCompanyOwnership(currentCompanyId, project.getContractorCompany().getId());
    }

    public void validateWorkPackageOwnership(Long currentCompanyId, WorkPackage workPackage) {
        if (workPackage == null || workPackage.getProject() == null) {
            throw new UnauthorizedOperationException("The work-package owner could not be identified.");
        }
        validateProjectOwnership(currentCompanyId, workPackage.getProject());
    }

    public void validateBidOwnership(Long currentCompanyId, Bid bid) {
        if (bid == null || bid.getBidderCompany() == null) {
            throw new UnauthorizedOperationException("The bid owner could not be identified.");
        }
        validateCompanyOwnership(currentCompanyId, bid.getBidderCompany().getId());
    }

    public void validateAdminOrCompanyOwner(
            boolean administrator,
            Long currentCompanyId,
            Long resourceCompanyId
    ) {
        if (!administrator) {
            validateCompanyOwnership(currentCompanyId, resourceCompanyId);
        }
    }

    private void requireId(Long id, String message) {
        if (id == null) {
            throw new UnauthorizedOperationException(message);
        }
    }
}
