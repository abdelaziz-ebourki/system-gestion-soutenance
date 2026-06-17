package com.system_gestion_soutenance.api.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkCreateRequest(@NotEmpty List<@Valid BulkUserEntry> users, @NotBlank String role) {
	public record BulkUserEntry(@NotBlank String lastName, @NotBlank String firstName, @NotBlank String email,
			String cne, String codeApogee, String majorName, String levelName, String teacherRankName,
			String departmentName) {
	}
}
