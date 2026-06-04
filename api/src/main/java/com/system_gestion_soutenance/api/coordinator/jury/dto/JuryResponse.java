package com.system_gestion_soutenance.api.coordinator.jury.dto;

import java.util.List;

public record JuryResponse(Long id, Long projectId, String projectTitle, String defenseType, Long templateId,
		String templateName, List<MemberResponse> members) {
	public record MemberResponse(String roleName, Long teacherId, String teacherName) {
	}
}
