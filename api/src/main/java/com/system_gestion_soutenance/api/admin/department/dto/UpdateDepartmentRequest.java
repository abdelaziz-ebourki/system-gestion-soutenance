package com.system_gestion_soutenance.api.admin.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update an existing department")
@SuppressWarnings("PMD")
public record UpdateDepartmentRequest(
		@Schema(description = "Name of the department", example = "Computer Science") String name,
		@Schema(description = "Unique code for the department", example = "CS") String code,
		@Schema(description = "ID of the department head", example = "1") Long headId,
		@Schema(description = "ID of the faculty this department belongs to", example = "1") Long facultyId) {
}
