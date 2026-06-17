package com.system_gestion_soutenance.api.coordinator.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête de proposition de projet par un enseignant")
public record TeacherProposeProjectRequest(
		@Schema(description = "Titre du projet", example = "Projet IA") @NotBlank(message = "Le titre du projet est obligatoire") String title,
		@Schema(description = "Description du projet", example = "Développement d'un système intelligent") @NotBlank(message = "La description du projet est obligatoire") String description,
		@Schema(description = "Type de soutenance", example = "PFE") @NotNull(message = "Le type de soutenance est obligatoire") String defenseType,
		@Schema(description = "Nombre maximal d'étudiants", example = "4") Integer maxStudents) {
}
