package com.system_gestion_soutenance.api.student.defense.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse contenant la note et la mention de l'étudiant")
@SuppressWarnings("PMD")
public record StudentGradeResponse(@Schema(description = "Note moyenne pondérée", example = "15.5") double score,
		@Schema(description = "Mention obtenue", example = "Bien") String mention) {
}
