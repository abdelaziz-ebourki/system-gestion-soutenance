package com.system_gestion_soutenance.api.coordinator.conflict.dto;

public record ConflictDetailResponse(String type, String severity, String message, String slot,
		String suggestedResolution) {
}
