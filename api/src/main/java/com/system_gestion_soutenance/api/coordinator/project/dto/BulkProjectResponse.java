package com.system_gestion_soutenance.api.coordinator.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse d'import massive de projet")
public record BulkProjectResponse(@Schema(description = "Identifiant du projet créé", example = "1") Long id,
		@Schema(description = "Titre du projet", example = "Projet IA") String title) {
}
