package com.system_gestion_soutenance.api.coordinator.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requête de mise à jour de projet")
public record UpdateProjectRequest(@Schema(description = "Titre du projet", example = "Nouveau titre") String title,
		@Schema(description = "Description du projet", example = "Nouvelle description") String description,
		@Schema(description = "Type de soutenance", example = "PFE") String defenseType) {
}
