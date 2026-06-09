package com.system_gestion_soutenance.api.admin.defensesession.dto;

import java.time.LocalDate;
import java.util.Map;
@SuppressWarnings("PMD")

public record DefenseSessionDto(Long id, String name, String defenseType, String status, int maxGroupSize,
		int defenseDuration, int breakDuration, LocalDate submissionDeadline,
		Map<String, Integer> evaluationCoefficients, Long juryRoleTemplateId, LocalDate startDate, LocalDate endDate) {
}