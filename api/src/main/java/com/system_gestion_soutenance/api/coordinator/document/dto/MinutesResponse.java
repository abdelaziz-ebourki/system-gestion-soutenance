package com.system_gestion_soutenance.api.coordinator.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse du procès-verbal de soutenance")
@SuppressWarnings("PMD")
public record MinutesResponse(@Schema(description = "Paramètres de l'institution") Settings settings,
		@Schema(description = "Détails de la note du projet") GradeDetails grade,
		@Schema(description = "Noms des étudiants", example = "[\"Alice Martin\", \"Bob Durand\"]") List<String> studentNames,
		@Schema(description = "Nom de l'enseignant encadrant", example = "Dr. Dupont") String supervisorName,
		@Schema(description = "Membres du jury") List<JuryMemberDetails> juryMembers) {

	@Schema(description = "Paramètres de l'institution")
	public record Settings(
			@Schema(description = "Nom de l'institution", example = "Université de Technologie") String institutionName,
			@Schema(description = "URL du logo de l'institution", example = "https://example.com/logo.png") String institutionLogoUrl,
			@Schema(description = "Fuseau horaire", example = "Europe/Paris") String timezone,
			@Schema(description = "Format de date", example = "dd/MM/yyyy") String dateFormat) {
	}

	@Schema(description = "Détails de la note du projet")
	public record GradeDetails(@Schema(description = "ID du projet", example = "1") Long projectId,
			@Schema(description = "Titre du projet", example = "Projet IA") String projectTitle,
			@Schema(description = "Score final", example = "15.5") double finalScore,
			@Schema(description = "Décision du jury", example = "Admis") String decision) {
	}

	@Schema(description = "Détails d'un membre du jury")
	public record JuryMemberDetails(@Schema(description = "Rôle dans le jury", example = "Rapporteur") String roleName,
			@Schema(description = "Nom de l'enseignant", example = "Dr. Dupont") String teacherName) {
	}
}
