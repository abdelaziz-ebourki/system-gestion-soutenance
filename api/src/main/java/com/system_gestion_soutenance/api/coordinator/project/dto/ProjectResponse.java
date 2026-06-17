package com.system_gestion_soutenance.api.coordinator.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse détaillée du projet")
@SuppressWarnings("PMD")
public record ProjectResponse(@Schema(description = "Identifiant du projet", example = "1") Long id,
		@Schema(description = "Titre du projet", example = "Projet IA") String title,
		@Schema(description = "Description du projet", example = "Développement d'un système intelligent") String description,
		@Schema(description = "Type de soutenance", example = "PFE") String defenseType,
		@Schema(description = "Statut du projet", example = "IN_PROGRESS") String status,
		@Schema(description = "Identifiant du groupe", example = "1") Long groupId,
		@Schema(description = "Nom de l'enseignant encadrant", example = "Dr. Dupont") String supervisorName,
		@Schema(description = "Noms des étudiants", example = "[\"Alice Martin\", \"Bob Durand\"]") List<String> studentNames,
		@Schema(description = "Liste des co-encadrants") List<CoSupervisorDto> coSupervisors) {

	@Schema(description = "Co-encadrant")
	public record CoSupervisorDto(@Schema(description = "Identifiant", example = "1") Long id,
			@Schema(description = "Nom complet", example = "Dr. Dupont") String name,
			@Schema(description = "Email", example = "dupont@univh2c.ma") String email) {
	}
}
