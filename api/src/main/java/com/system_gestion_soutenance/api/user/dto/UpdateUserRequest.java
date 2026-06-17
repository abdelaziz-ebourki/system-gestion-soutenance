package com.system_gestion_soutenance.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request for updating user information")
public record UpdateUserRequest(@Schema(description = "User's last name", example = "Doe") String lastName,
		@Schema(description = "User's first name", example = "John") String firstName,
		@Schema(description = "User's email address", example = "john.doe@example.com") String email,
		@Schema(description = "User role", example = "STUDENT") String role,
		@Schema(description = "Student's national student number (CNE)", example = "12345678") String cne,
		@Schema(description = "Student's Apogee code", example = "APG12345") String codeApogee,
		@Schema(description = "Major ID", example = "1") Long majorId,
		@Schema(description = "Level ID", example = "1") Long levelId,
		@Schema(description = "Teacher rank ID", example = "1") Long teacherRankId,
		@Schema(description = "Department ID", example = "1") Long departmentId) {
}
