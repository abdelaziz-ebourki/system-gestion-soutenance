package com.system_gestion_soutenance.api.coordinator.document.dto;

import java.util.List;
@SuppressWarnings("PMD")

public record MinutesResponse(Settings settings, GradeDetails grade, List<String> studentNames, String supervisorName,
		List<JuryMemberDetails> juryMembers) {
	public record Settings(String institutionName, String institutionLogoUrl, String timezone, String dateFormat) {
	}
	public record GradeDetails(Long projectId, String projectTitle, double finalScore, String decision) {
	}
	public record JuryMemberDetails(String roleName, String teacherName) {
	}
}