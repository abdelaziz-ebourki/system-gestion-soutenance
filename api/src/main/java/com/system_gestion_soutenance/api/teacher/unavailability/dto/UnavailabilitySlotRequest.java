package com.system_gestion_soutenance.api.teacher.unavailability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Requête de créneau d'indisponibilité pour une date donnée")
@SuppressWarnings("PMD")
public record UnavailabilitySlotRequest(
		@Schema(description = "Date du créneau d'indisponibilité", example = "2026-06-15") @NotBlank(message = "La date est obligatoire") String date,
		@Schema(description = "Liste des créneaux horaires indisponibles", example = "[\"08:00-10:00\", \"14:00-16:00\"]") @NotNull(message = "Les créneaux sont obligatoires") List<String> slots) {
}
