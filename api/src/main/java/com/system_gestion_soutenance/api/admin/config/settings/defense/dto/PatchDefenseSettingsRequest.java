package com.system_gestion_soutenance.api.admin.config.settings.defense.dto;

@SuppressWarnings("PMD")

public record PatchDefenseSettingsRequest(String startTime, String endTime, Integer defenseDuration,
		Integer breakDuration, String groupCreationStartDate, String groupCreationEndDate) {
}
