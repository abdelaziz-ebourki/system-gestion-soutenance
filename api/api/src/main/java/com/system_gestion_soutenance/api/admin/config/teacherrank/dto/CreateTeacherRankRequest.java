package com.system_gestion_soutenance.api.admin.config.teacherrank.dto;

import com.system_gestion_soutenance.api.common.dto.NamedRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new teacher rank")
public record CreateTeacherRankRequest(
		@Schema(description = "Name of the teacher rank", example = "Professor") @NotBlank String name)
		implements
			NamedRequest {
}
