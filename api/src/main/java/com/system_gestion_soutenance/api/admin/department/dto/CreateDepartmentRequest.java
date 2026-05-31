package com.system_gestion_soutenance.api.admin.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDepartmentRequest(@NotBlank String name, @NotBlank String code, Long headId,
		@NotNull Long facultyId) {
}
