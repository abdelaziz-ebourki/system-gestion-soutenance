package com.system_gestion_soutenance.api.teacher.unavailability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête de création d'indisponibilité pour un enseignant")
@SuppressWarnings("PMD")
public record CreateUnavailabilityRequest(
		@Schema(description = "Date de début de l'indisponibilité", example = "2026-06-15") @NotNull(message = "La date de début est obligatoire") String startDate,
		@Schema(description = "Date de fin de l'indisponibilité", example = "2026-06-20") @NotNull(message = "La date de fin est obligatoire") String endDate,
		@Schema(description = "Motif de l'indisponibilité", example = "Congé personnel") @NotBlank(message = "Le motif est obligatoire") String reason) {
}
