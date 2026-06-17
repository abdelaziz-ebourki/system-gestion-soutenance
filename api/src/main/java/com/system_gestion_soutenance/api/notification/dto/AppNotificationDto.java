package com.system_gestion_soutenance.api.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "DTO d'une notification de l'application")
@SuppressWarnings("PMD")
public record AppNotificationDto(@Schema(description = "Identifiant unique de la notification", example = "1") Long id,
		@Schema(description = "Type de notification", example = "DEFENSE_SCHEDULED") String type,
		@Schema(description = "Titre de la notification", example = "Soutenance planifiée") String title,
		@Schema(description = "Contenu de la notification", example = "Votre soutenance est prévue le 15 juin 2026") String message,
		@Schema(description = "Horodatage de la notification", example = "2026-06-14T10:30:00") LocalDateTime timestamp,
		@Schema(description = "Indique si la notification a été lue", example = "false") boolean read,
		@Schema(description = "Identifiant de l'utilisateur cible (null = global)", example = "1") Long userId,
		@Schema(description = "Lien d'action associé à la notification", example = "/defenses/1") String actionLink,
		@Schema(description = "Nom de l'acteur ayant déclenché la notification", example = "Dr. Ahmed Ben Salah") String actor) {
}
