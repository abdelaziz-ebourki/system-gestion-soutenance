package com.system_gestion_soutenance.api.admin.config.settings.defense.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update defense settings")
@SuppressWarnings("PMD")
public record UpdateDefenseSettingsRequest(
		@Schema(description = "Start time for defense sessions", example = "08:00") @NotBlank String startTime,
		@Schema(description = "End time for defense sessions", example = "17:00") @NotBlank String endTime,
		@Schema(description = "Duration of each defense in minutes", example = "30") @Min(1) int defenseDuration,
		@Schema(description = "Duration of breaks between defenses in minutes", example = "10") @Min(0) int breakDuration,
		@Schema(description = "Start date for group creation", example = "2026-01-15") String groupCreationStartDate,
		@Schema(description = "End date for group creation", example = "2026-02-01") String groupCreationEndDate) {
}
