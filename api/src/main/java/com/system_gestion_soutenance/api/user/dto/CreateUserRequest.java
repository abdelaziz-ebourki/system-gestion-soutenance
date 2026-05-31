package com.system_gestion_soutenance.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@NotBlank String lastName, @NotBlank String firstName, @NotBlank @Email String email,
		String role, String cne, Long majorId, Long levelId, Long gradeId, Long departmentId) {
}
