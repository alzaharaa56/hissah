package com.hissah.Controllers;

import com.hissah.DTO.Request.AdminBootstrapRequestDTO;
import com.hissah.Services.AdminBootstrapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class AdminBootstrapController {

    private final AdminBootstrapService adminBootstrapService;

    @PostMapping("/admin")
    public ResponseEntity<Map<String, Object>> createFirstAdmin(
            @RequestHeader("X-Setup-Key")
            String setupKey,
            @Valid @RequestBody
            AdminBootstrapRequestDTO request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        adminBootstrapService.createFirstAdmin(
                                request,
                                setupKey
                        )
                );
    }
}
