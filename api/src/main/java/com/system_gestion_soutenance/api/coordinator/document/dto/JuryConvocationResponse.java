package com.system_gestion_soutenance.api.coordinator.document.dto;

import java.util.List;

public record JuryConvocationResponse(String teacherName, String role, String projectTitle, List<String> studentNames,
		String date, String time, String roomName, String defenseSessionName) {
}
