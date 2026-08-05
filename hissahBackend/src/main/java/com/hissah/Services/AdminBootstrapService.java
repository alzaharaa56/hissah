package com.hissah.Services;

import com.hissah.DTO.Request.AdminBootstrapRequestDTO;

import java.util.Map;

public interface AdminBootstrapService {

    Map<String, Object> createFirstAdmin(
            AdminBootstrapRequestDTO request,
            String suppliedSetupKey
    );
}
