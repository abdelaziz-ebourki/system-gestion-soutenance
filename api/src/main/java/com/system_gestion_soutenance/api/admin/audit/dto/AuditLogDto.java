package com.system_gestion_soutenance.api.admin.audit.dto;

import java.time.LocalDateTime;

public record AuditLogDto(Long id, String action, String entity, Long entityId, String adminEmail, String details,
		LocalDateTime timestamp) {
}
