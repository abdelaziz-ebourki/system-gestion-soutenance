package com.system_gestion_soutenance.api.coordinator.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Requête contenant les IDs de défenses")
@SuppressWarnings("PMD")
public record DefenseIdsRequest(
		@Schema(description = "Liste des IDs de défenses", example = "[1, 2, 3]") List<Long> defenseIds,
		@Schema(description = "ID du projet", example = "1") @NotNull Long projectId) {
	public DefenseIdsRequest {
		if (defenseIds == null)
			defenseIds = List.of();
	}
}
