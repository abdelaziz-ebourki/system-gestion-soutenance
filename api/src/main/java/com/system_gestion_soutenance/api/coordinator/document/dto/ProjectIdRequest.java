package com.system_gestion_soutenance.api.coordinator.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête contenant l'ID du projet pour documents")
public record ProjectIdRequest(
		@Schema(description = "ID du projet", example = "1") @NotNull(message = "Le projet est obligatoire") Long projectId) {
}
