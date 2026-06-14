package com.system_gestion_soutenance.api.coordinator.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête de session de soutenance pour documents")
public record SessionRequest(
		@Schema(description = "ID de la session de soutenance", example = "1") @NotNull(message = "La session de soutenance est obligatoire") Long defenseSessionId) {
}
