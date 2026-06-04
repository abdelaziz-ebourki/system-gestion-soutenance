package com.system_gestion_soutenance.api.coordinator.schedule.dto;

import jakarta.validation.constraints.NotNull;

public record SlotAssignmentRequest(String title, @NotNull(message = "La date est obligatoire") String date,
		@NotNull(message = "L'heure est obligatoire") String time, Long projectId, Long roomId) {
}
