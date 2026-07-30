package com.hissah.Controllers;

import com.hissah.DTO.Request.ProjectRequestDTO;
import com.hissah.DTO.Response.ProjectResponseDTO;
import com.hissah.DTO.Response.ProjectSummaryResponseDTO;
import com.hissah.Enums.Role;
import com.hissah.Services.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(
            @RequestBody ProjectRequestDTO request,
            @RequestParam Long contractorCompanyId,
            @RequestParam Long currentUserId
    ) {
        ProjectResponseDTO response = projectService.create(request, contractorCompanyId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long projectId,
            @RequestBody ProjectRequestDTO request,
            @RequestParam Long contractorCompanyId,
            @RequestParam Long currentUserId
    ) {
        ProjectResponseDTO response = projectService.update(projectId, request, contractorCompanyId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{projectId}/activate")
    public ResponseEntity<ProjectResponseDTO> activateProject(
            @PathVariable Long projectId,
            @RequestParam Long contractorCompanyId,
            @RequestParam Long currentUserId
    ) {
        ProjectResponseDTO response = projectService.activate(projectId, contractorCompanyId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{projectId}/complete")
    public ResponseEntity<ProjectResponseDTO> completeProject(
            @PathVariable Long projectId,
            @RequestParam Long contractorCompanyId,
            @RequestParam Long currentUserId
    ) {
        ProjectResponseDTO response = projectService.complete(projectId, contractorCompanyId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{projectId}/cancel")
    public ResponseEntity<ProjectResponseDTO> cancelProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) String reason,
            @RequestParam Long contractorCompanyId,
            @RequestParam Long currentUserId
    ) {
        ProjectResponseDTO response = projectService.cancel(projectId, reason, contractorCompanyId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(
            @PathVariable Long projectId,
            @RequestParam Long currentCompanyId,
            @RequestParam Role currentRole
    ) {
        ProjectResponseDTO response = projectService.getById(projectId, currentCompanyId, currentRole);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-projects")
    public ResponseEntity<Page<ProjectSummaryResponseDTO>> getMyProjects(
            @RequestParam Long contractorCompanyId,
            Pageable pageable
    ) {
        Page<ProjectSummaryResponseDTO> response = projectService.getMyProjects(contractorCompanyId, pageable);
        return ResponseEntity.ok(response);
    }
}
