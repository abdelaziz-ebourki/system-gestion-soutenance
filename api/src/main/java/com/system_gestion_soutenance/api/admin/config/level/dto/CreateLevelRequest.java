package com.system_gestion_soutenance.api.admin.config.level.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateLevelRequest(@NotBlank String name) {
}
