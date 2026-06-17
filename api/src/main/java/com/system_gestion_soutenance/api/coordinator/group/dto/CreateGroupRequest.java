package com.system_gestion_soutenance.api.coordinator.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Requête de création de groupe")
public record CreateGroupRequest(
		@Schema(description = "Nom du groupe", example = "Groupe Alpha") @NotBlank(message = "Le nom du groupe est obligatoire") String groupName,
		@Schema(description = "ID du projet associé", example = "1") @NotNull(message = "Le projet est obligatoire") Long projectId,
		@Schema(description = "Liste des IDs des étudiants", example = "[1, 2, 3]") List<Long> studentIds,
		@Schema(description = "ID de la session de soutenance", example = "1") Long sessionId,
		@Schema(description = "ID du chef de groupe", example = "1") Long leaderId) {
}
