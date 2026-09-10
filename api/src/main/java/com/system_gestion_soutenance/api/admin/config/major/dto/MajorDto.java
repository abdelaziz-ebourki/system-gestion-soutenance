package com.system_gestion_soutenance.api.admin.config.major.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing major details")
@SuppressWarnings("PMD")
public record MajorDto(@Schema(description = "Unique identifier of the major", example = "1") Long id,
		@Schema(description = "Name of the major", example = "Software Engineering") String name,
		@Schema(description = "ID of the department this major belongs to", example = "1") Long departmentId,
		@Schema(description = "Name of the department this major belongs to", example = "Computer Science") String departmentName,
		@Schema(description = "Number of students enrolled in this major", example = "120") long studentCount) {
}
