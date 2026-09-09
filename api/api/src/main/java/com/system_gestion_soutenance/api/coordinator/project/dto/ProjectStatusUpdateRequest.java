package com.system_gestion_soutenance.api.coordinator.project.dto;

import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête de mise à jour du statut du projet")
public record ProjectStatusUpdateRequest(
		@Schema(description = "Nouveau statut du projet", example = "IN_PROGRESS") @NotNull ProjectStatus status) {
}
