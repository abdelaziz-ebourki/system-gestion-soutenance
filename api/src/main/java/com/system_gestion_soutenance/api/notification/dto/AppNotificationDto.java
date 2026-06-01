package com.system_gestion_soutenance.api.notification.dto;

import java.time.LocalDateTime;

public record AppNotificationDto(Long id, String type, String title, String message, LocalDateTime timestamp,
		boolean read, String actionLink, String actor) {
}
