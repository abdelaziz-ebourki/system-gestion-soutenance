package com.system_gestion_soutenance.api.admin.config.teacherrank.dto;

import com.system_gestion_soutenance.api.common.dto.NamedRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update an existing teacher rank")
@SuppressWarnings("PMD")
public record UpdateTeacherRankRequest(
		@Schema(description = "Name of the teacher rank", example = "Professor") String name) implements NamedRequest {
}
