package com.system_gestion_soutenance.api.coordinator.group.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateGroupRequest(String groupName, @NotNull Long projectId, List<Long> studentIds, Long sessionId) {
}
