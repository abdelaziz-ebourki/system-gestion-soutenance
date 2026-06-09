package com.system_gestion_soutenance.api.coordinator.defensesession.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
@SuppressWarnings("PMD")

public record CreateDefenseSessionRequest(@NotBlank String name, @NotBlank String defenseType, String status,
		int maxGroupSize, int defenseDuration, int breakDuration, String submissionDeadline,
		Map<String, Integer> evaluationCoefficients, Long juryRoleTemplateId, @NotBlank String startDate,
		@NotBlank String endDate) {
}