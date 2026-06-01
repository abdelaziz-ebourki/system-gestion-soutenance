package com.system_gestion_soutenance.api.user.dto;

public record UserDto(Long id, String email, String role, String lastName, String firstName, boolean isActive,
		String cne, Long majorId, String majorName, Long levelId, String levelName, Long gradeId, String gradeName,
		Long departmentId, String departmentName) {
}
