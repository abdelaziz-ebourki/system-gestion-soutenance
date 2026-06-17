package com.system_gestion_soutenance.api.teacher.evaluation.dto;

import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationAttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

@Schema(description = "Requête de soumission d'évaluation pour un projet de soutenance")
@SuppressWarnings("PMD")
public record EvaluationSubmitRequest(
		@Schema(description = "Note finale du projet sur 20", example = "15.5") @DecimalMin("0.0") @DecimalMax("20.0") Double score,
		@Schema(description = "Commentaire de l'évaluateur", example = "Bon travail, résultats satisfaisants") String comment,
		@Schema(description = "Statut de présence du candidat", example = "PRESENT") EvaluationAttendanceStatus attendanceStatus) {
}
