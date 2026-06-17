package com.system_gestion_soutenance.api.admin.config.major.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new major")
public record CreateMajorRequest(
		@Schema(description = "Name of the major", example = "Software Engineering") @NotBlank String name,
		@Schema(description = "ID of the department this major belongs to", example = "1") Long departmentId) {
}
