package com.system_gestion_soutenance.api.teacher.stats.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse contenant les statistiques de l'enseignant")
@SuppressWarnings("PMD")
public record TeacherStatsResponse(
		@Schema(description = "Nombre de soutenances à venir", example = "5") int upcomingDefenses,
		@Schema(description = "Nombre d'évaluations en attente", example = "3") long pendingEvaluations,
		@Schema(description = "Nombre de créneaux d'indisponibilité déclarés", example = "2") long declaredUnavailabilitySlots,
		@Schema(description = "Nombre de jurys auxquels l'enseignant est assigné", example = "4") long juryAssignments) {
}
