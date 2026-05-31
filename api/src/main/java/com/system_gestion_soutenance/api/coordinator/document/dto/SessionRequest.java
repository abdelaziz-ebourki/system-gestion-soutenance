package com.system_gestion_soutenance.api.coordinator.document.dto;

import jakarta.validation.constraints.NotNull;

public record SessionRequest(@NotNull Long defenseSessionId) {
}
