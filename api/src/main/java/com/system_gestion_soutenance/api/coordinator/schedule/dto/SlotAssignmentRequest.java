package com.system_gestion_soutenance.api.coordinator.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête d'assignation de créneau")
public record SlotAssignmentRequest(
		@Schema(description = "Titre du créneau", example = "Soutenance Groupe Alpha") String title,
		@Schema(description = "Date de la soutenance", example = "2025-06-15") @NotNull(message = "La date est obligatoire") String date,
		@Schema(description = "Heure de la soutenance", example = "09:00") @NotNull(message = "L'heure est obligatoire") String time,
		@Schema(description = "ID du projet", example = "1") Long projectId,
		@Schema(description = "ID de la salle", example = "1") Long roomId) {
}
