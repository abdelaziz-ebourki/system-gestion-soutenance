package com.system_gestion_soutenance.api.coordinator.grade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
@SuppressWarnings("PMD")

@Schema(description = "Requête de délibération d'une session de soutenance")
public record DeliberationRequest(
		@Schema(description = "Notes finales par projet (projectId -> score)", example = "{\"1\": 15.5, \"2\": 12.0}") @NotNull Map<Long, Double> finalScores,
		@Schema(description = "Commentaire de délibération", example = "Session validée") String comment) {
}
