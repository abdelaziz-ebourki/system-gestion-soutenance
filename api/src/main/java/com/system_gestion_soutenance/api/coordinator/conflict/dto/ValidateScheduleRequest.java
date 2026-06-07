package com.system_gestion_soutenance.api.coordinator.conflict.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.SlotAssignmentRequest;

public record ValidateScheduleRequest(
		@NotNull(message = "Le champ 'defenseSessionId' est requis") Long defenseSessionId,
		@NotNull(message = "Le planning est requis") List<SlotAssignmentRequest> schedule) {
}
