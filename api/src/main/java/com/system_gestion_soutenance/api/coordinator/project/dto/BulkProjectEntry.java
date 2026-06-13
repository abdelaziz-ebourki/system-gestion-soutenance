package com.system_gestion_soutenance.api.coordinator.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BulkProjectEntry(@NotBlank(message = "Le titre du projet est obligatoire") String title,
		@NotBlank(message = "La description du projet est obligatoire") String description,
		@NotNull(message = "L'enseignant encadrant est obligatoire") Long supervisorId,
		@NotNull(message = "Le type de soutenance est obligatoire") String defenseType, List<Long> studentIds) {
}
