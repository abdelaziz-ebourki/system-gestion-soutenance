package com.system_gestion_soutenance.api.coordinator.document.dto;

import java.util.List;
@SuppressWarnings("PMD")

public record EvaluationSheetResponse(Long projectId, String projectTitle, List<String> studentNames,
		String supervisorName, String date, String time, String roomName, List<JuryMemberResponse> juryMembers,
		java.util.Map<String, Integer> evaluationCoefficients) {
	public record JuryMemberResponse(String roleName, String teacherName, int coefficient) {
	}
}