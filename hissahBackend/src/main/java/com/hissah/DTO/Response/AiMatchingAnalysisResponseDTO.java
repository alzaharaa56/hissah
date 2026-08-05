package com.hissah.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiMatchingAnalysisResponseDTO {

    private List<AiMatchingAnalysisItemDTO> matches = new ArrayList<>();
}
