package com.system_gestion_soutenance.api.coordinator.conflict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.SlotAssignmentRequest;

@Schema(description = "Requête de validation de planning")
public record ValidateScheduleRequest(
		@Schema(description = "ID de la session de soutenance", example = "1") @NotNull(message = "Le champ 'defenseSessionId' est requis") Long defenseSessionId,
		@Schema(description = "Planning à valider") @NotNull(message = "Le planning est requis") List<SlotAssignmentRequest> schedule) {
}
