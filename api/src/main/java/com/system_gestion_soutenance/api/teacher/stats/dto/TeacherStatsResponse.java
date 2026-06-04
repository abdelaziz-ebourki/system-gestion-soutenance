package com.system_gestion_soutenance.api.teacher.stats.dto;

public record TeacherStatsResponse(int upcomingDefenses, long pendingEvaluations, long declaredUnavailabilitySlots,
		long juryAssignments) {
}
