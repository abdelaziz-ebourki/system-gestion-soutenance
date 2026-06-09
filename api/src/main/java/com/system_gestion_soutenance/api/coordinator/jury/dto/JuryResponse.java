package com.system_gestion_soutenance.api.coordinator.jury.dto;

import java.util.List;
@SuppressWarnings("PMD")

public record JuryResponse(Long id, Long projectId, String projectTitle, String defenseType,
		List<MemberResponse> members) {
	public record MemberResponse(String roleName, Long teacherId, String teacherName) {
	}
}