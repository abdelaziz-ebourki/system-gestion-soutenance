package com.system_gestion_soutenance.api.coordinator.document.dto;

import jakarta.validation.constraints.NotNull;

public record SessionRequest(@NotNull(message = "La session de soutenance est obligatoire") Long defenseSessionId) {
}
