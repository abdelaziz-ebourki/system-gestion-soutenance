package com.system_gestion_soutenance.api.admin.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuditLogRequest(@NotBlank String action, @NotBlank String entity, @NotNull Long entityId,
		@NotBlank String adminEmail, String details) {
}
