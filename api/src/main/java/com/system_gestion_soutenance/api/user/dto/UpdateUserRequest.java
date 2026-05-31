package com.system_gestion_soutenance.api.user.dto;

public record UpdateUserRequest(String lastName, String firstName, String email, String role, String cne, Long majorId,
		Long levelId, Long gradeId, Long departmentId) {
}
