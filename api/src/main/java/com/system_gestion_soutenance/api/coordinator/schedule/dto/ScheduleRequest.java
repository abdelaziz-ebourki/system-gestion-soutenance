package com.system_gestion_soutenance.api.coordinator.schedule.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ScheduleRequest(@NotNull(message = "La session de soutenance est obligatoire") Long defenseSessionId,
		@NotNull(message = "Les créneaux sont obligatoires") List<SlotAssignmentRequest> slots) {
}
