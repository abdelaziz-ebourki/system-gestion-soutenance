package com.system_gestion_soutenance.api.admin.config.major.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update an existing major")
@SuppressWarnings("PMD")
public record UpdateMajorRequest(
		@Schema(description = "Name of the major", example = "Software Engineering") String name,
		@Schema(description = "ID of the department this major belongs to", example = "1") Long departmentId) {
}
