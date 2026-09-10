package com.system_gestion_soutenance.api.coordinator.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
@SuppressWarnings("PMD")

@Schema(description = "Rapport de session (PV de session)")
public record SessionReportResponse(@Schema(description = "Nom de la session") String sessionName,
		@Schema(description = "Type de soutenance") String defenseType,
		@Schema(description = "Date de début") String startDate, @Schema(description = "Date de fin") String endDate,
		@Schema(description = "Nombre total de projets") int totalProjects,
		@Schema(description = "Nombre de projets réussis") int passedProjects,
		@Schema(description = "Taux de réussite") double passRate,
		@Schema(description = "Détails des défenses") List<DefenseReportDetails> defenses) {

	@Schema(description = "Détails d'une défense dans le rapport")
	public record DefenseReportDetails(@Schema(description = "Titre du projet") String projectTitle,
			@Schema(description = "Noms des étudiants") List<String> studentNames,
			@Schema(description = "Date") String date, @Schema(description = "Heure") String time,
			@Schema(description = "Salle") String room,
			@Schema(description = "Membres du jury") List<String> juryMembers,
			@Schema(description = "Note finale") Double finalScore, @Schema(description = "Mention") String mention) {
	}
}
