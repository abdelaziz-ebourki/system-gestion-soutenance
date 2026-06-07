package com.system_gestion_soutenance.api.coordinator.project.dto;

import java.util.List;

public record ProjectResponse(Long id, String title, String description, String defenseType, Long groupId,
		String supervisorName, List<String> studentNames) {
}
