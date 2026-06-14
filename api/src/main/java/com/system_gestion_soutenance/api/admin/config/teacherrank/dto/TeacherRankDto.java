package com.system_gestion_soutenance.api.admin.config.teacherrank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing teacher rank details")
@SuppressWarnings("PMD")
public record TeacherRankDto(@Schema(description = "Unique identifier of the teacher rank", example = "1") Long id,
		@Schema(description = "Name of the teacher rank", example = "Professor") String name) {
}
