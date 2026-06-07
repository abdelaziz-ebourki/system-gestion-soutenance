package com.system_gestion_soutenance.api.coordinator.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateGroupRequest(@NotBlank(message = "Le nom du groupe est obligatoire") String groupName,
		@NotNull(message = "Le projet est obligatoire") Long projectId, List<Long> studentIds, Long sessionId) {
}
