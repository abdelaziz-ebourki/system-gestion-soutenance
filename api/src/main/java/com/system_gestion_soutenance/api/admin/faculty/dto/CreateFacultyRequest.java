package com.system_gestion_soutenance.api.admin.faculty.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new faculty")
public record CreateFacultyRequest(
		@Schema(description = "Name of the faculty", example = "Faculty of Sciences") @NotBlank String name,
		@Schema(description = "Unique code for the faculty", example = "FS") @NotBlank String code,
		@Schema(description = "ID of the dean of the faculty", example = "1") Long deanId,
		@Schema(description = "URL of the faculty logo", example = "https://example.com/logo.png") String logoUrl) {
}
