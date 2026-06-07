package com.system_gestion_soutenance.api.coordinator.grade.dto;

import java.util.List;
import java.util.Map;

public record GradeWeightedAverageResponse(Long projectId, String projectTitle, String defenseDate, String status,
		Double finalScore, Map<String, Integer> evaluationCoefficients,
		List<IndividualScoreResponse> individualScores) {
}
