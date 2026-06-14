package com.system_gestion_soutenance.api.coordinator.defensesession.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requête de transition de statut de session")
public record StatusTransitionRequest(
		@Schema(description = "Statut cible de la transition", example = "ACTIVE") @NotBlank(message = "Le nouveau statut est obligatoire") String toStatus) {
}
