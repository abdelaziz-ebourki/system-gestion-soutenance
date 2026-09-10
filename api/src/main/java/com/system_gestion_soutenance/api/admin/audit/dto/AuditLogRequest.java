package com.system_gestion_soutenance.api.admin.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to create a new audit log entry")
public record AuditLogRequest(@Schema(description = "Action performed", example = "CREATE") @NotBlank String action,
		@Schema(description = "Entity type affected", example = "Department") @NotBlank String entity,
		@Schema(description = "ID of the affected entity", example = "1") @NotNull Long entityId,
		@Schema(description = "Email of the user who performed the action", example = "admin@example.com") @NotBlank String performedByEmail,
		@Schema(description = "Additional details about the action", example = "Department created successfully") String details) {
}
