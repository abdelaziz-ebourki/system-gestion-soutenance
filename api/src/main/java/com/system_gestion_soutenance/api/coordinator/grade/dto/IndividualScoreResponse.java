package com.system_gestion_soutenance.api.coordinator.grade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse d'un score individuel du jury")
public record IndividualScoreResponse(
		@Schema(description = "Rôle dans le jury", example = "Rapporteur") String roleName,
		@Schema(description = "Nom de l'enseignant", example = "Dr. Dupont") String teacherName,
		@Schema(description = "Note attribuée par l'enseignant", example = "15.0") Double score) {
}
