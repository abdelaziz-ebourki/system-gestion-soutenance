package com.system_gestion_soutenance.api.coordinator.schedule.dto;

import jakarta.validation.constraints.NotNull;

public record DefenseSessionIdRequest(
		@NotNull(message = "Le champ 'defenseSessionId' est requis") Long defenseSessionId) {
}
