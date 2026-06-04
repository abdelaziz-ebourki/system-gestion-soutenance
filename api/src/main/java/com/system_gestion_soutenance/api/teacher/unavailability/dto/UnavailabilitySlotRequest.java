package com.system_gestion_soutenance.api.teacher.unavailability.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UnavailabilitySlotRequest(@NotBlank(message = "La date est obligatoire") String date,
		@NotNull(message = "Les créneaux sont obligatoires") java.util.List<String> slots) {
}
