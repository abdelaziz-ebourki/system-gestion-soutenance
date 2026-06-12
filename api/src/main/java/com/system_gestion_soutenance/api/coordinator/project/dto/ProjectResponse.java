package com.system_gestion_soutenance.api.coordinator.project.dto;

import java.util.List;
@SuppressWarnings("PMD")

public record ProjectResponse(Long id, String title, String description, String defenseType, String status,
		Long groupId, String supervisorName, List<String> studentNames) {
}