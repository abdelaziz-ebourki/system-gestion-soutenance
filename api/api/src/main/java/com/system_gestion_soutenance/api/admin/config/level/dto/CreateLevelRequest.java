package com.system_gestion_soutenance.api.admin.config.level.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new level")
public record CreateLevelRequest(
		@Schema(description = "Name of the level", example = "Master 1") @NotBlank String name) {
}
