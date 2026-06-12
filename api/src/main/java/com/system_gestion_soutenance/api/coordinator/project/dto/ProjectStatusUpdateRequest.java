package com.system_gestion_soutenance.api.coordinator.project.dto;

import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record ProjectStatusUpdateRequest(@NotNull ProjectStatus status) {
}
