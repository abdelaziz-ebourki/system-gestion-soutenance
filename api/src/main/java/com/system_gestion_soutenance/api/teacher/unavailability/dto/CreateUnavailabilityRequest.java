package com.system_gestion_soutenance.api.teacher.unavailability.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUnavailabilityRequest(@NotNull(message = "La date de début est obligatoire") String startDate,
		@NotNull(message = "La date de fin est obligatoire") String endDate,
		@NotBlank(message = "Le motif est obligatoire") String reason) {
}
