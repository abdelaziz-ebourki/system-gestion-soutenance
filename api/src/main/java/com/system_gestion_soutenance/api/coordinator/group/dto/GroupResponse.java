package com.system_gestion_soutenance.api.coordinator.group.dto;

import java.util.List;
@SuppressWarnings("PMD")

public record GroupResponse(Long id, String groupName, Long projectId, int memberCount, List<String> studentNames) {
}