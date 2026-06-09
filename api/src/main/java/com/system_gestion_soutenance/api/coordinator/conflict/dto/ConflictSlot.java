package com.system_gestion_soutenance.api.coordinator.conflict.dto;
@SuppressWarnings("PMD")

public record ConflictSlot(String id, String title, String date, String time, String endTime, String projectId,
		String roomId) {
}