package com.system_gestion_soutenance.api.teacher.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse contenant le planning de soutenance de l'enseignant")
@SuppressWarnings("PMD")
public record TeacherScheduleResponse(
		@Schema(description = "Liste des créneaux de soutenance assignés à l'enseignant") List<SlotDetails> slots) {
}
