package com.system_gestion_soutenance.api.coordinator.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateProjectRequest(@NotBlank String title, String description, @NotNull Long supervisorId,
		List<Long> studentIds, @NotBlank String defenseType) {
}
