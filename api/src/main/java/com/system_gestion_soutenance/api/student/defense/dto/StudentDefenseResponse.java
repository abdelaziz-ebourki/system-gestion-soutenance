package com.system_gestion_soutenance.api.student.defense.dto;

import java.util.List;
@SuppressWarnings("PMD")

public record StudentDefenseResponse(String projectTitle, String projectDescription, String supervisorName,
		List<JuryMemberResponse> juryMembers, String date, String startTime, String endTime, String roomName,
		String status, String convocationUrl, String result) {
}