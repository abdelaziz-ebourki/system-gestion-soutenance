package com.system_gestion_soutenance.api.coordinator.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse du document de planning")
@SuppressWarnings("PMD")
public record ScheduleDocResponse(
		@Schema(description = "Nom de la session de soutenance", example = "Session Juin 2025") String defenseSessionName,
		@Schema(description = "Liste des créneaux de soutenance") List<SlotDetails> slots) {
}
