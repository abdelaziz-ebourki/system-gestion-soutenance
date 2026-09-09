package com.system_gestion_soutenance.api.admin.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new department")
public record CreateDepartmentRequest(
		@Schema(description = "Name of the department", example = "Computer Science") @NotBlank String name,
		@Schema(description = "Unique code for the department", example = "CS") @NotBlank String code,
		@Schema(description = "ID of the department head", example = "1") Long headId,
		@Schema(description = "ID of the faculty this department belongs to", example = "1") Long facultyId) {
}
