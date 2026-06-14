package com.system_gestion_soutenance.api.coordinator.defensesession.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@Schema(description = "Requête de création de session de soutenance")
@SuppressWarnings("PMD")
public record CreateDefenseSessionRequest(
		@Schema(description = "Nom de la session", example = "Session Juin 2025") @NotBlank String name,
		@Schema(description = "Type de soutenance", example = "PFE") @NotBlank String defenseType,
		@Schema(description = "Statut initial de la session", example = "DRAFT") String status,
		@Schema(description = "Taille maximale du groupe", example = "5") int maxGroupSize,
		@Schema(description = "Durée de la soutenance en minutes", example = "30") int defenseDuration,
		@Schema(description = "Durée de la pause en minutes", example = "10") int breakDuration,
		@Schema(description = "Date limite de soumission", example = "2025-06-01T23:59:59") String submissionDeadline,
		@Schema(description = "Coefficients d'évaluation", example = "{\"Technique\": 4, \"Présentation\": 3}") Map<String, Integer> evaluationCoefficients,
		@Schema(description = "ID du modèle de rôle jury", example = "1") Long juryRoleTemplateId,
		@Schema(description = "Date de début de la session", example = "2025-06-15") @NotBlank String startDate,
		@Schema(description = "Date de fin de la session", example = "2025-06-30") @NotBlank String endDate) {
}
