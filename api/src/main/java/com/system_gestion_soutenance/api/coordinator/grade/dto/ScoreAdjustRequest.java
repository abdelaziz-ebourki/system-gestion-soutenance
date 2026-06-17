package com.system_gestion_soutenance.api.coordinator.grade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
@SuppressWarnings("PMD")

@Schema(description = "Requête d'ajustement de note")
public record ScoreAdjustRequest(
		@Schema(description = "Nouvelle note finale", example = "14.0") @NotNull @Min(0) @Max(20) Double finalScore,
		@Schema(description = "Commentaire d'ajustement", example = "Ajustement après délibération") String comment) {
}
