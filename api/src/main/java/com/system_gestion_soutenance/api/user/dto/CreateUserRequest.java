package com.system_gestion_soutenance.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request for creating a new user")
public record CreateUserRequest(@Schema(description = "User's last name", example = "Doe") @NotBlank String lastName,
		@Schema(description = "User's first name", example = "John") @NotBlank String firstName,
		@Schema(description = "User's email address", example = "john.doe@example.com") @NotBlank @Email String email,
		@Schema(description = "User role (STUDENT, TEACHER, COORDINATOR, ADMIN)", example = "STUDENT") String role,
		@Schema(description = "Student's national student number (CNE)", example = "12345678") String cne,
		@Schema(description = "Student's Apogee code", example = "APG12345") String codeApogee,
		@Schema(description = "Major ID (for students)", example = "1") Long majorId,
		@Schema(description = "Level ID (for students)", example = "1") Long levelId,
		@Schema(description = "Teacher rank ID (for teachers)", example = "1") Long teacherRankId,
		@Schema(description = "Department ID (for teachers)", example = "1") Long departmentId) {
}
