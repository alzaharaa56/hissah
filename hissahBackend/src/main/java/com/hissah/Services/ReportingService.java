package com.hissah.Services;

import com.hissah.DTO.Response.DashboardResponseDTO;
import com.hissah.Enums.Role;

public interface ReportingService {
    DashboardResponseDTO getDashboard(
            Long currentUserId,
            Long currentCompanyId,
            Role role);
}
