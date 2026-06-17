package com.system_gestion_soutenance.api.coordinator.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Requête de planification de soutenance")
public record ScheduleRequest(
		@Schema(description = "ID de la session de soutenance", example = "1") @NotNull(message = "La session de soutenance est obligatoire") Long defenseSessionId,
		@Schema(description = "Créneaux à planifier") @NotNull(message = "Les créneaux sont obligatoires") List<SlotAssignmentRequest> slots) {
}
