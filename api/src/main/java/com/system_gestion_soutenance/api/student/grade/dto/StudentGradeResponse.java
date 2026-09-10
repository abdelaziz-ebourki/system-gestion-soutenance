package com.system_gestion_soutenance.api.student.grade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
@SuppressWarnings("PMD")

@Schema(description = "Note de l'étudiant")
public record StudentGradeResponse(@Schema(description = "ID du projet") Long projectId,
		@Schema(description = "Titre du projet") String projectTitle,
		@Schema(description = "Date de soutenance") String defenseDate,
		@Schema(description = "Note finale") Double finalScore, @Schema(description = "Mention") String mention,
		@Schema(description = "Statut (published ou pending)") String status,
		@Schema(description = "Scores individuels des jurys") List<String> juryScores) {
}
