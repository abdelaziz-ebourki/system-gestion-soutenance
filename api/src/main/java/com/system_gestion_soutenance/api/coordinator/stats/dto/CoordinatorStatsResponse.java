package com.system_gestion_soutenance.api.coordinator.stats.dto;

public record CoordinatorStatsResponse(long totalProjects, long totalGroups, long totalJuries, long scheduledDefenses) {
}
