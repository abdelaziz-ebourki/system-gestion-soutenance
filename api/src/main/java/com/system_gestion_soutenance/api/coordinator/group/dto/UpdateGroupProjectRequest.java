package com.system_gestion_soutenance.api.coordinator.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête de mise à jour du projet d'un groupe")
public record UpdateGroupProjectRequest(
		@Schema(description = "ID du projet à assigner", example = "1") @NotNull(message = "Le projet est obligatoire") Long projectId) {
}
