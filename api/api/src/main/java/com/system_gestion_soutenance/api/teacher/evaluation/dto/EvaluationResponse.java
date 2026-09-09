package com.system_gestion_soutenance.api.teacher.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse contenant les détails d'une évaluation de soutenance")
@SuppressWarnings("PMD")
public record EvaluationResponse(@Schema(description = "Identifiant unique de l'évaluation", example = "1") Long id,
		@Schema(description = "Identifiant du projet évalué", example = "10") Long projectId,
		@Schema(description = "Titre du projet évalué", example = "Système de gestion de soutenance") String projectTitle,
		@Schema(description = "Note finale du projet sur 20", example = "15.5") Double finalGrade,
		@Schema(description = "Commentaire de l'évaluateur", example = "Excellent travail") String comment,
		@Schema(description = "Statut de l'évaluation", example = "VALIDE") String status,
		@Schema(description = "Statut de présence du candidat", example = "PRESENT") String attendanceStatus,
		@Schema(description = "Type d'évaluation", example = "SOUTENANCE") String type) {
}
