package com.system_gestion_soutenance.api.admin.audit.dto;

import java.time.LocalDateTime;
@SuppressWarnings("PMD")

public record AuditLogDto(Long id, String action, String entity, Long entityId, String performedByEmail, String details,
		LocalDateTime timestamp) {
}