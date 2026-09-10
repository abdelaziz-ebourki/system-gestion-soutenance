package com.system_gestion_soutenance.api.coordinator.grade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(description = "Réponse de la moyenne pondérée des notes")
@SuppressWarnings("PMD")
public record GradeWeightedAverageResponse(@Schema(description = "ID du projet", example = "1") Long projectId,
		@Schema(description = "Titre du projet", example = "Projet IA") String projectTitle,
		@Schema(description = "Date de la soutenance", example = "2025-06-15") String defenseDate,
		@Schema(description = "Statut de la note", example = "FINAL") String status,
		@Schema(description = "Score final pondéré", example = "15.5") Double finalScore,
		@Schema(description = "Coefficients d'évaluation", example = "{\"Technique\": 4, \"Présentation\": 3}") Map<String, Integer> evaluationCoefficients,
		@Schema(description = "Scores individuels des membres du jury") List<IndividualScoreResponse> individualScores) {
}
