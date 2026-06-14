package com.system_gestion_soutenance.api.coordinator.unavailability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "DTO d'indisponibilité d'enseignant")
@SuppressWarnings("PMD")
public record UnavailabilityDto(@Schema(description = "Identifiant de l'indisponibilité", example = "1") Long id,
		@Schema(description = "ID de l'enseignant", example = "1") Long teacherId,
		@Schema(description = "Date d'indisponibilité", example = "2025-06-15") String date,
		@Schema(description = "Créneaux indisponibles", example = "[\"09:00-12:00\", \"14:00-17:00\"]") List<String> slots) {
}
