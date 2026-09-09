package com.system_gestion_soutenance.api.admin.config.level.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing level details")
@SuppressWarnings("PMD")
public record LevelDto(@Schema(description = "Unique identifier of the level", example = "1") Long id,
		@Schema(description = "Name of the level", example = "Master 1") String name) {
}
