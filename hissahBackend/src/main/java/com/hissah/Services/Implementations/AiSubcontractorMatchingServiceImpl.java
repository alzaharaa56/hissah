package com.hissah.Services.Implementations;

import com.hissah.Configurations.AiProperties;
import com.hissah.DTO.Response.AiMatchingAnalysisItemDTO;
import com.hissah.DTO.Response.AiMatchingAnalysisResponseDTO;
import com.hissah.DTO.Response.AiSubcontractorMatchResponseDTO;
import com.hissah.Entities.Category;
import com.hissah.Entities.Company;
import com.hissah.Entities.CompanyCategory;
import com.hissah.Entities.Project;
import com.hissah.Entities.WorkPackage;
import com.hissah.Enums.AwardStatus;
import com.hissah.Enums.CompanyType;
import com.hissah.Enums.MilestoneStatus;
import com.hissah.Enums.VerificationStatus;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Exceptions.UnauthorizedOperationException;
import com.hissah.Repositories.AwardRepository;
import com.hissah.Repositories.CategoryRepository;
import com.hissah.Repositories.CompanyCategoryRepository;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Repositories.MilestoneRepository;
import com.hissah.Repositories.WorkPackageRepository;
import com.hissah.Services.AiClientService;
import com.hissah.Services.AiSubcontractorMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiSubcontractorMatchingServiceImpl
        implements AiSubcontractorMatchingService {

    private final WorkPackageRepository workPackageRepository;
    private final CompanyRepository companyRepository;
    private final CompanyCategoryRepository companyCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final AwardRepository awardRepository;
    private final MilestoneRepository milestoneRepository;
    private final AiClientService aiClientService;
    private final AiProperties aiProperties;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    @Transactional(readOnly = true)
    public List<AiSubcontractorMatchResponseDTO> findMatches(
            Long workPackageId,
            Long contractorCompanyId,
            int limit
    ) {
        if (!aiProperties.isEnabled()) {
            throw new BusinessRuleException(
                    "AI features are disabled. Set AI_ENABLED=true."
            );
        }

        int safeLimit = Math.max(1, Math.min(limit, 10));

        WorkPackage workPackage = workPackageRepository
                .findByIdAndActiveTrue(workPackageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work package not found with id: " + workPackageId
                ));

        Project project = workPackage.getProject();

        if (project == null || project.getContractorCompanyId() == null) {
            throw new BusinessRuleException(
                    "The work package is not linked to a valid contractor project."
            );
        }

        Long ownerCompanyId = project.getContractorCompanyId();

        if (!ownerCompanyId.equals(contractorCompanyId)) {
            throw new UnauthorizedOperationException(
                    "You can only request matches for your own work packages."
            );
        }

        int candidateLimit = Math.max(
                safeLimit,
                aiProperties.getMatchingCandidateLimit()
        );

        List<Candidate> candidates = companyRepository
                .findAll()
                .stream()
                .filter(company -> Boolean.TRUE.equals(company.getActive()))
                .filter(company -> company.getVerificationStatus()
                        == VerificationStatus.VERIFIED)
                .filter(company -> company.getCompanyType()
                        == CompanyType.SUBCONTRACTOR
                        || company.getCompanyType() == CompanyType.BOTH)
                .filter(company -> !company.getId().equals(contractorCompanyId))
                .map(company -> buildCandidate(workPackage, company))
                .sorted(Comparator.comparingInt(Candidate::baseScore).reversed())
                .limit(candidateLimit)
                .toList();

        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<Long, AiMatchingAnalysisItemDTO> aiAnalysis =
                aiProperties.isMockEnabled()
                        ? createMockAnalysis(candidates)
                        : requestAiAnalysis(workPackage, candidates);

        return candidates.stream()
                .map(candidate -> toResponse(
                        candidate,
                        aiAnalysis.get(candidate.company().getId()),
                        aiProperties.isMockEnabled()
                ))
                .sorted(
                        Comparator.comparingInt(
                                AiSubcontractorMatchResponseDTO::getMatchScore
                        ).reversed()
                )
                .limit(safeLimit)
                .toList();
    }

    private Candidate buildCandidate(
            WorkPackage workPackage,
            Company company
    ) {
        List<CompanyCategory> companyCategoryLinks =
                companyCategoryRepository.findByCompanyId(company.getId());

        List<Long> categoryIds = companyCategoryLinks
                .stream()
                .map(CompanyCategory::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> categoryNamesById = categoryRepository
                .findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        Category::getName,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        List<String> categoryNames = categoryIds
                .stream()
                .map(categoryNamesById::get)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        Category workPackageCategory = workPackage.getCategory();

        if (workPackageCategory == null
                || workPackageCategory.getId() == null) {
            throw new BusinessRuleException(
                    "The work package has no valid category."
            );
        }

        boolean categoryMatch = categoryIds.contains(
                workPackageCategory.getId()
        );

        boolean locationMatch = locationMatches(
                workPackage.getLocation(),
                company.getGovernorate()
        );

        long completedAwards = awardRepository
                .countByBidBidderCompanyIdAndStatusAndActiveTrue(
                        company.getId(),
                        AwardStatus.COMPLETED
                );

        long approvedMilestones = milestoneRepository
                .countByAwardBidBidderCompanyIdAndStatusAndActiveTrue(
                        company.getId(),
                        MilestoneStatus.APPROVED
                );

        int baseScore = 0;

        if (categoryMatch) {
            baseScore += 40;
        }

        if (locationMatch) {
            baseScore += 15;
        }

        // Only verified companies reach this point.
        baseScore += 10;
        baseScore += (int) Math.min(5, completedAwards * 2);
        baseScore += (int) Math.min(5, approvedMilestones);

        List<String> deterministicReasons = new ArrayList<>();

        if (categoryMatch) {
            deterministicReasons.add(
                    "The company is registered in the work package category."
            );
        }

        if (locationMatch) {
            deterministicReasons.add(
                    "The company operates in a matching location."
            );
        }

        deterministicReasons.add("The company is verified on Hissah.");

        if (completedAwards > 0) {
            deterministicReasons.add(
                    "The company has completed previous awards on the platform."
            );
        }

        if (approvedMilestones > 0) {
            deterministicReasons.add(
                    "The company has approved milestone history."
            );
        }

        return new Candidate(
                company,
                categoryNames,
                baseScore,
                completedAwards,
                approvedMilestones,
                deterministicReasons
        );
    }

    private Map<Long, AiMatchingAnalysisItemDTO> requestAiAnalysis(
            WorkPackage workPackage,
            List<Candidate> candidates
    ) {
        String instructions = """
                You are the Hissah AI Subcontractor Matching Assistant for Oman.
                Compare the work package with each candidate company.
                Use only the supplied company profile, categories, location and platform history.
                Do not invent experience, certificates, staff, clients or completed projects.
                Give each candidate a semantic score from 0 to 30.
                The score measures how well the company description and categories fit the work scope.
                Return concise reasons and honest possible gaps.
                """;

        List<Map<String, Object>> candidatePayload = candidates
                .stream()
                .map(candidate -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("companyId", candidate.company().getId());
                    payload.put("legalName", candidate.company().getLegalName());
                    payload.put("tradingName", candidate.company().getTradingName());
                    payload.put("governorate", candidate.company().getGovernorate());
                    payload.put("description", candidate.company().getDescription());
                    payload.put("categories", candidate.categories());
                    payload.put("baseScore", candidate.baseScore());
                    payload.put("completedAwards", candidate.completedAwards());
                    payload.put("approvedMilestones", candidate.approvedMilestones());
                    return payload;
                })
                .toList();

        String workPackageCategoryName = workPackage.getCategory() == null
                ? null
                : workPackage.getCategory().getName();

        Map<String, Object> inputPayload = new LinkedHashMap<>();
        inputPayload.put("workPackageId", workPackage.getId());
        inputPayload.put("title", workPackage.getTitle());
        inputPayload.put("scope", workPackage.getScope());
        inputPayload.put("requirements", workPackage.getRequirements());
        inputPayload.put("location", workPackage.getLocation());
        inputPayload.put("category", workPackageCategoryName);
        inputPayload.put("candidates", candidatePayload);

        try {
            String input = jsonMapper.writeValueAsString(inputPayload);

            AiMatchingAnalysisResponseDTO response =
                    aiClientService.generateStructuredResponse(
                            instructions,
                            input,
                            "hissah_subcontractor_matches",
                            matchingSchema(),
                            AiMatchingAnalysisResponseDTO.class
                    );

            if (response == null || response.getMatches() == null) {
                return Map.of();
            }

            return response.getMatches()
                    .stream()
                    .filter(item -> item.getCompanyId() != null)
                    .collect(Collectors.toMap(
                            AiMatchingAnalysisItemDTO::getCompanyId,
                            Function.identity(),
                            (first, second) -> first
                    ));

        } catch (BusinessRuleException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessRuleException(
                    "The matching request could not be prepared for the AI provider."
            );
        }
    }

    private Map<Long, AiMatchingAnalysisItemDTO> createMockAnalysis(
            List<Candidate> candidates
    ) {
        Map<Long, AiMatchingAnalysisItemDTO> result = new LinkedHashMap<>();

        for (Candidate candidate : candidates) {
            int semanticScore = candidate.categories().isEmpty() ? 6 : 14;

            List<String> reasons = new ArrayList<>(
                    candidate.deterministicReasons()
            );
            reasons.add(
                    "The available profile information is generally relevant to the requested work."
            );

            List<String> gaps = new ArrayList<>();

            if (candidate.company().getDescription() == null
                    || candidate.company().getDescription().isBlank()) {
                gaps.add(
                        "The company profile contains limited experience information."
                );
            }

            result.put(
                    candidate.company().getId(),
                    new AiMatchingAnalysisItemDTO(
                            candidate.company().getId(),
                            semanticScore,
                            reasons,
                            gaps
                    )
            );
        }

        return result;
    }

    private AiSubcontractorMatchResponseDTO toResponse(
            Candidate candidate,
            AiMatchingAnalysisItemDTO analysis,
            boolean mockResponse
    ) {
        int semanticScore = analysis == null
                || analysis.getSemanticScore() == null
                ? 0
                : Math.max(0, Math.min(30, analysis.getSemanticScore()));

        int finalScore = Math.min(
                100,
                candidate.baseScore() + semanticScore
        );

        List<String> reasons = analysis == null
                || analysis.getReasons() == null
                || analysis.getReasons().isEmpty()
                ? candidate.deterministicReasons()
                : analysis.getReasons();

        List<String> gaps = analysis == null
                || analysis.getPossibleGaps() == null
                ? List.of()
                : analysis.getPossibleGaps();

        return AiSubcontractorMatchResponseDTO.builder()
                .companyId(candidate.company().getId())
                .legalName(candidate.company().getLegalName())
                .tradingName(candidate.company().getTradingName())
                .governorate(candidate.company().getGovernorate())
                .categories(candidate.categories())
                .baseScore(candidate.baseScore())
                .aiSemanticScore(semanticScore)
                .matchScore(finalScore)
                .matchReasons(reasons)
                .possibleGaps(gaps)
                .completedAwards(candidate.completedAwards())
                .approvedMilestones(candidate.approvedMilestones())
                .mockResponse(mockResponse)
                .build();
    }

    private Map<String, Object> matchingSchema() {
        Map<String, Object> stringArray = Map.of(
                "type", "array",
                "items", Map.of("type", "string")
        );

        Map<String, Object> itemProperties = new LinkedHashMap<>();
        itemProperties.put("companyId", Map.of("type", "integer"));
        itemProperties.put(
                "semanticScore",
                Map.of(
                        "type", "integer",
                        "minimum", 0,
                        "maximum", 30
                )
        );
        itemProperties.put("reasons", stringArray);
        itemProperties.put("possibleGaps", stringArray);

        Map<String, Object> itemSchema = new LinkedHashMap<>();
        itemSchema.put("type", "object");
        itemSchema.put("properties", itemProperties);
        itemSchema.put(
                "required",
                List.of(
                        "companyId",
                        "semanticScore",
                        "reasons",
                        "possibleGaps"
                )
        );
        itemSchema.put("additionalProperties", false);

        Map<String, Object> rootProperties = new LinkedHashMap<>();
        rootProperties.put(
                "matches",
                Map.of(
                        "type", "array",
                        "items", itemSchema
                )
        );

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", rootProperties);
        schema.put("required", List.of("matches"));
        schema.put("additionalProperties", false);
        return schema;
    }

    private boolean locationMatches(
            String workLocation,
            String companyGovernorate
    ) {
        String left = normalize(workLocation);
        String right = normalize(companyGovernorate);

        return !left.isBlank()
                && !right.isBlank()
                && (left.contains(right) || right.contains(left));
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private record Candidate(
            Company company,
            List<String> categories,
            int baseScore,
            long completedAwards,
            long approvedMilestones,
            List<String> deterministicReasons
    ) {
    }
}
