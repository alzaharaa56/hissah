package com.hissah.Services.Implementations;

import com.hissah.Configurations.AiProperties;
import com.hissah.DTO.Request.AiWorkPackageRequestDTO;
import com.hissah.DTO.Response.AiWorkPackageResponseDTO;
import com.hissah.Entities.Category;
import com.hissah.Repositories.CategoryRepository;
import com.hissah.Services.AiClientService;
import com.hissah.Services.AiWorkPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiWorkPackageServiceImpl
        implements AiWorkPackageService {

    private final AiClientService aiClientService;
    private final AiProperties properties;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public AiWorkPackageResponseDTO generateDraft(
            AiWorkPackageRequestDTO request
    ) {
        if (!properties.isEnabled()) {
            throw new com.hissah.Exceptions.BusinessRuleException(
                    "AI features are disabled. Set AI_ENABLED=true."
            );
        }

        String categoryName = resolveCategoryName(request.getCategoryId());

        if (properties.isMockEnabled()) {
            return createMockDraft(request, categoryName);
        }

        String instructions = """
                You are the Hissah AI Work Package Assistant for an Omani subcontracting platform.
                Create a clear, professional and practical subcontracting work-package draft.
                Use only the information supplied by the user.
                Do not invent government approvals, certificates, standards, quantities or technical facts.
                Keep the title concise and keep all content editable by the contractor.
                Write in clear professional English.
                """;

        String input = """
                Contractor brief:
                %s

                Context:
                - Category: %s
                - Location: %s
                - Minimum budget: %s OMR
                - Maximum budget: %s OMR
                - Deadline: %s

                Generate a work package draft containing a title, scope, requirements,
                deliverables, evaluation criteria, safety requirements, risk notes,
                and a realistic suggested duration in days.
                """.formatted(
                safe(request.getBrief()),
                categoryName,
                safe(request.getLocation()),
                value(request.getBudgetMin()),
                value(request.getBudgetMax()),
                value(request.getDeadline())
        );

        AiWorkPackageResponseDTO response =
                aiClientService.generateStructuredResponse(
                        instructions,
                        input,
                        "hissah_work_package_draft",
                        workPackageSchema(),
                        AiWorkPackageResponseDTO.class
                );

        response.setMockResponse(false);
        return response;
    }

    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "Not selected";
        }

        return categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse("Unknown category ID " + categoryId);
    }

    private AiWorkPackageResponseDTO createMockDraft(
            AiWorkPackageRequestDTO request,
            String categoryName
    ) {
        String conciseBrief = request.getBrief().trim();
        String title = createTitle(conciseBrief, categoryName);

        return AiWorkPackageResponseDTO.builder()
                .title(title)
                .scope(
                        "The selected subcontractor will plan, execute, test and hand over the required work described as: "
                                + conciseBrief
                                + ". The work must be coordinated with the main contractor and completed at "
                                + safe(request.getLocation())
                                + "."
                )
                .requirements(
                        "The bidder must provide a clear method statement, proposed schedule, responsible team details, "
                                + "commercial offer, relevant experience, and any documents required by the main contractor."
                )
                .deliverables(List.of(
                        "Approved work plan and schedule",
                        "Completed scope of work",
                        "Testing or inspection evidence where applicable",
                        "Final handover report"
                ))
                .evaluationCriteria(List.of(
                        "Relevant experience",
                        "Technical approach",
                        "Price and value",
                        "Proposed completion period"
                ))
                .safetyRequirements(List.of(
                        "Follow the site safety instructions",
                        "Use suitable personal protective equipment",
                        "Report hazards and incidents to the main contractor"
                ))
                .riskNotes(List.of(
                        "Material or resource availability may affect the schedule",
                        "Site access and coordination requirements should be confirmed"
                ))
                .suggestedDurationDays(30)
                .mockResponse(true)
                .build();
    }

    private String createTitle(String brief, String categoryName) {
        String cleaned = brief
                .replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.length() > 70) {
            cleaned = cleaned.substring(0, 70).trim();
        }

        if (!"Not selected".equals(categoryName)
                && !categoryName.startsWith("Unknown")) {
            return categoryName + " - " + cleaned;
        }

        return cleaned;
    }

    private Map<String, Object> workPackageSchema() {
        Map<String, Object> stringArray = Map.of(
                "type", "array",
                "items", Map.of("type", "string")
        );

        Map<String, Object> propertiesMap = new LinkedHashMap<>();
        propertiesMap.put("title", Map.of("type", "string"));
        propertiesMap.put("scope", Map.of("type", "string"));
        propertiesMap.put("requirements", Map.of("type", "string"));
        propertiesMap.put("deliverables", stringArray);
        propertiesMap.put("evaluationCriteria", stringArray);
        propertiesMap.put("safetyRequirements", stringArray);
        propertiesMap.put("riskNotes", stringArray);
        propertiesMap.put(
                "suggestedDurationDays",
                Map.of(
                        "type", "integer",
                        "minimum", 1,
                        "maximum", 365
                )
        );
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", propertiesMap);
        schema.put(
                "required",
                List.of(
                        "title",
                        "scope",
                        "requirements",
                        "deliverables",
                        "evaluationCriteria",
                        "safetyRequirements",
                        "riskNotes",
                        "suggestedDurationDays"
                )
        );
        schema.put("additionalProperties", false);
        return schema;
    }

    private String safe(Object value) {
        return value == null || value.toString().isBlank()
                ? "Not supplied"
                : value.toString();
    }

    private String value(Object value) {
        return value == null ? "Not supplied" : value.toString();
    }
}
