package com.system_gestion_soutenance.api.coordinator.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse de la fiche d'évaluation")
@SuppressWarnings("PMD")
public record EvaluationSheetResponse(@Schema(description = "ID du projet", example = "1") Long projectId,
		@Schema(description = "Titre du projet", example = "Projet IA") String projectTitle,
		@Schema(description = "Noms des étudiants", example = "[\"Alice Martin\", \"Bob Durand\"]") List<String> studentNames,
		@Schema(description = "Nom de l'enseignant encadrant", example = "Dr. Dupont") String supervisorName,
		@Schema(description = "Date de la soutenance", example = "2025-06-15") String date,
		@Schema(description = "Heure de la soutenance", example = "09:00") String time,
		@Schema(description = "Nom de la salle", example = "Salle A101") String roomName,
		@Schema(description = "Membres du jury") List<JuryMemberResponse> juryMembers,
		@Schema(description = "Coefficients d'évaluation", example = "{\"Technique\": 4, \"Présentation\": 3}") java.util.Map<String, Integer> evaluationCoefficients) {

	@Schema(description = "Réponse d'un membre du jury dans la fiche d'évaluation")
	public record JuryMemberResponse(@Schema(description = "Rôle dans le jury", example = "Rapporteur") String roleName,
			@Schema(description = "Nom de l'enseignant", example = "Dr. Dupont") String teacherName,
			@Schema(description = "Coefficient d'évaluation", example = "3") int coefficient) {
	}
}
