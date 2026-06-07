package com.system_gestion_soutenance.api.coordinator.schedule.dto;

import java.util.List;

public record ScheduleResponse(Long id, String title, String date, String time, Long projectId, Long roomId,
		String roomName, String projectTitle, List<String> studentNames, String role, String status) {
}
