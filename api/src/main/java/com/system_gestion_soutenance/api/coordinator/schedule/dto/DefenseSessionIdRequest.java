package com.system_gestion_soutenance.api.coordinator.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête contenant l'ID de la session de soutenance")
public record DefenseSessionIdRequest(
		@Schema(description = "ID de la session de soutenance", example = "1") @NotNull(message = "Le champ 'defenseSessionId' est requis") Long defenseSessionId) {
}
