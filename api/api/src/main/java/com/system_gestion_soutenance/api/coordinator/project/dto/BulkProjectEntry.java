package com.system_gestion_soutenance.api.coordinator.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Schema(description = "Entrée de projet pour l'import massive")
public record BulkProjectEntry(
		@Schema(description = "Titre du projet", example = "Projet IA") @NotBlank(message = "Le titre du projet est obligatoire") String title,
		@Schema(description = "Description du projet", example = "Développement d'un système intelligent") @NotBlank(message = "La description du projet est obligatoire") String description,
		@Schema(description = "ID de l'enseignant encadrant", example = "1") Long supervisorId,
		@Schema(description = "Email de l'enseignant encadrant (alternative à supervisorId)", example = "prof@example.com") String supervisorEmail,
		@Schema(description = "Type de soutenance", example = "PFE") @NotBlank(message = "Le type de soutenance est obligatoire") String defenseType,
		@Schema(description = "Liste des IDs des étudiants", example = "[1, 2, 3]") List<Long> studentIds) {
}
