package com.system_gestion_soutenance.api.admin.config.settings.defense.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateDefenseSettingsRequest(@NotBlank String startTime, @NotBlank String endTime,
		@Min(1) int defenseDuration, @Min(0) int breakDuration, String groupCreationStartDate,
		String groupCreationEndDate) {
}
