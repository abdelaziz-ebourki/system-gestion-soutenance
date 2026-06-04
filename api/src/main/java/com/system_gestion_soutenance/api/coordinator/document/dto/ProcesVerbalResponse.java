package com.system_gestion_soutenance.api.coordinator.document.dto;

import java.util.List;
import java.util.Map;

public record ProcesVerbalResponse(Map<String, Object> settings, GradeDetails grade, List<String> studentNames,
		String supervisorName, List<JuryMemberDetails> juryMembers) {
	public record GradeDetails(Long projectId, String projectTitle, double finalScore, String decision) {
	}
	public record JuryMemberDetails(String roleName, String teacherName) {
	}
}
