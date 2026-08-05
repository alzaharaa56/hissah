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
public class AiWorkPackageResponseDTO {

    private String title;
    private String scope;
    private String requirements;

    @Builder.Default
    private List<String> deliverables = new ArrayList<>();

    @Builder.Default
    private List<String> evaluationCriteria = new ArrayList<>();

    @Builder.Default
    private List<String> safetyRequirements = new ArrayList<>();

    @Builder.Default
    private List<String> riskNotes = new ArrayList<>();

    private Integer suggestedDurationDays;
    private Boolean mockResponse;
}
