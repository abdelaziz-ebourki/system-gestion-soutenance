package com.system_gestion_soutenance.api.teacher.schedule.dto;
@SuppressWarnings("PMD")

public record SlotDetails(Long id, Long projectId, String projectTitle, java.util.List<String> studentNames,
		String date, String startTime, String endTime, String roomName, String role, String status) {
}