package com.system_gestion_soutenance.api.coordinator.conflict.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse détaillée de conflit de planning")
@SuppressWarnings("PMD")
public record ConflictDetailResponse(@Schema(description = "Type de conflit", example = "ROOM_OVERLAP") String type,
		@Schema(description = "Gravité du conflit", example = "HIGH") String severity,
		@Schema(description = "Message de description du conflit", example = "Conflit de salle détecté") String message,
		@Schema(description = "Créneau concerné par le conflit", example = "2025-06-15 09:00") String slot,
		@Schema(description = "Résolution suggérée", example = "Déplacer vers salle B202") String suggestedResolution) {
}
