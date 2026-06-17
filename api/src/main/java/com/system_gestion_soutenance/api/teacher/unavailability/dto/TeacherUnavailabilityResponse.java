package com.system_gestion_soutenance.api.teacher.unavailability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(description = "Réponse contenant les indisponibilités de l'enseignant regroupées par date")
@SuppressWarnings("PMD")
public record TeacherUnavailabilityResponse(
		@Schema(description = "Map des créneaux indisponibles regroupés par date", example = "{\"2026-06-15\": [\"08:00-10:00\", \"14:00-16:00\"]}") Map<String, List<String>> slotsByDate) {
}
