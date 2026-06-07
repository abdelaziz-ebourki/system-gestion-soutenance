package com.system_gestion_soutenance.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User data transfer object")
public record UserDto(@Schema(description = "Unique identifier of the user", example = "1") Long id,
		@Schema(description = "User's email address", example = "user@univh2c.ma") String email,
		@Schema(description = "User role", example = "STUDENT") String role,
		@Schema(description = "User's last name", example = "Doe") String lastName,
		@Schema(description = "User's first name", example = "John") String firstName,
		@Schema(description = "Account status", example = "true") boolean isActive,
		@Schema(description = "Student's national student number (CNE)", example = "12345678") String cne,
		@Schema(description = "Major ID", example = "1") Long majorId,
		@Schema(description = "Major name", example = "Computer Science") String majorName,
		@Schema(description = "Level ID", example = "1") Long levelId,
		@Schema(description = "Level name", example = "L3") String levelName,
		@Schema(description = "Grade ID", example = "1") Long gradeId,
		@Schema(description = "Grade name", example = "Professor") String gradeName,
		@Schema(description = "Department ID", example = "1") Long departmentId,
		@Schema(description = "Department name", example = "IT Department") String departmentName) {
}
