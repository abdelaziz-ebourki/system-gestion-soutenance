package com.system_gestion_soutenance.api.coordinator.defensesession.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusTransitionRequest(@NotBlank(message = "Le nouveau statut est obligatoire") String toStatus) {
}
