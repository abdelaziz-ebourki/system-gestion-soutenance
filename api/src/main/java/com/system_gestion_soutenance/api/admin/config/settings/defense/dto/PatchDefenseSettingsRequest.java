package com.system_gestion_soutenance.api.admin.config.settings.defense.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to partially update defense settings")
@SuppressWarnings("PMD")
public record PatchDefenseSettingsRequest(
		@Schema(description = "Start time for defense sessions", example = "08:00") String startTime,
		@Schema(description = "End time for defense sessions", example = "17:00") String endTime,
		@Schema(description = "Duration of each defense in minutes", example = "30") Integer defenseDuration,
		@Schema(description = "Duration of breaks between defenses in minutes", example = "10") Integer breakDuration,
		@Schema(description = "Start date for group creation", example = "2026-01-15") String groupCreationStartDate,
		@Schema(description = "End date for group creation", example = "2026-02-01") String groupCreationEndDate) {
}
