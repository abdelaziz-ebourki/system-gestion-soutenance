package com.system_gestion_soutenance.api.admin.config.general.dto;

public record UpdateGeneralSettingsRequest(String institutionName, String institutionLogoUrl, String timezone,
		String dateFormat, Boolean setupCompleted) {
}
