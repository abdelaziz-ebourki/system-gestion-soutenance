package com.system_gestion_soutenance.api.coordinator.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
@SuppressWarnings("PMD")

@Schema(description = "Historique des notes par projet")
public record GradeHistoryResponse(@Schema(description = "ID du projet") Long projectId,
		@Schema(description = "Titre du projet") String projectTitle,
		@Schema(description = "Évaluations individuelles") List<EvaluationEntry> evaluations,
		@Schema(description = "Moyenne calculée") Double computedAverage,
		@Schema(description = "Note finale") Double finalScore,
		@Schema(description = "Commentaire d'ajustement") String adjustmentComment,
		@Schema(description = "Date de délibération") LocalDateTime deliberatedAt,
		@Schema(description = "Date de validation") LocalDateTime validatedAt) {

	@Schema(description = "Évaluation individuelle d'un membre du jury")
	public record EvaluationEntry(@Schema(description = "Nom de l'enseignant") String teacher,
			@Schema(description = "Rôle") String role, @Schema(description = "Note") Double score,
			@Schema(description = "Date de soumission") LocalDateTime submittedAt) {
	}
}
