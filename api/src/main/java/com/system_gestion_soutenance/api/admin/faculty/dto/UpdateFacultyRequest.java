package com.system_gestion_soutenance.api.admin.faculty.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update an existing faculty")
@SuppressWarnings("PMD")
public record UpdateFacultyRequest(
		@Schema(description = "Name of the faculty", example = "Faculty of Sciences") String name,
		@Schema(description = "Unique code for the faculty", example = "FS") String code,
		@Schema(description = "ID of the dean of the faculty", example = "1") Long deanId,
		@Schema(description = "URL of the faculty logo", example = "https://example.com/logo.png") String logoUrl) {
}
