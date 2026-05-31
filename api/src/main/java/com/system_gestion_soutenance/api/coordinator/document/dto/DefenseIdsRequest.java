package com.system_gestion_soutenance.api.coordinator.document.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DefenseIdsRequest(List<Long> defenseIds, @NotNull Long projectId) {
	public DefenseIdsRequest {
		if (defenseIds == null)
			defenseIds = List.of();
	}
}
