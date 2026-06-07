package com.system_gestion_soutenance.api.coordinator.document.dto;

import jakarta.validation.constraints.NotNull;

public record ProjectIdRequest(@NotNull(message = "Le projet est obligatoire") Long projectId) {
}
