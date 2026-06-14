package com.system_gestion_soutenance.api.admin.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Response containing audit log details")
@SuppressWarnings("PMD")
public record AuditLogDto(@Schema(description = "Unique identifier of the audit log", example = "1") Long id,
		@Schema(description = "Action performed", example = "CREATE") String action,
		@Schema(description = "Entity type affected", example = "Department") String entity,
		@Schema(description = "ID of the affected entity", example = "1") Long entityId,
		@Schema(description = "Email of the user who performed the action", example = "admin@example.com") String performedByEmail,
		@Schema(description = "Additional details about the action", example = "Department created successfully") String details,
		@Schema(description = "Timestamp of the action", example = "2026-01-15T10:30:00") LocalDateTime timestamp) {
}
