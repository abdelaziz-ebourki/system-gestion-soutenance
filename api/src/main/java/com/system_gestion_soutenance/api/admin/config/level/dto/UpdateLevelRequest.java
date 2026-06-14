package com.system_gestion_soutenance.api.admin.config.level.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update an existing level")
@SuppressWarnings("PMD")
public record UpdateLevelRequest(@Schema(description = "Name of the level", example = "Master 1") String name) {
}
