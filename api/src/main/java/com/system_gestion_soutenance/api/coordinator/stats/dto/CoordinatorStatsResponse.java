package com.system_gestion_soutenance.api.coordinator.stats.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// Suppress PMD warnings for DTO record fields
@SuppressWarnings("PMD")
@Schema(description = "Réponse des statistiques du coordinateur")
public record CoordinatorStatsResponse(
		@Schema(description = "Nombre total de projets", example = "25") long totalProjects,
		@Schema(description = "Nombre total de groupes", example = "10") long totalGroups,
		@Schema(description = "Nombre total de jurys constitués", example = "8") long totalJuries,
		@Schema(description = "Nombre de soutenances planifiées", example = "6") long scheduledDefenses) {
}
