package com.hissah.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSubcontractorMatchResponseDTO {

    private Long companyId;
    private String legalName;
    private String tradingName;
    private String governorate;

    @Builder.Default
    private List<String> categories = new ArrayList<>();

    private Integer baseScore;
    private Integer aiSemanticScore;
    private Integer matchScore;

    @Builder.Default
    private List<String> matchReasons = new ArrayList<>();

    @Builder.Default
    private List<String> possibleGaps = new ArrayList<>();

    private Long completedAwards;
    private Long approvedMilestones;
    private Boolean mockResponse;
}
