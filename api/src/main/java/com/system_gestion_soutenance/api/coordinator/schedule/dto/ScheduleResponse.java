package com.system_gestion_soutenance.api.coordinator.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse de planification de soutenance")
@SuppressWarnings("PMD")
public record ScheduleResponse(@Schema(description = "Identifiant du créneau", example = "1") Long id,
		@Schema(description = "Titre du créneau", example = "Soutenance Groupe Alpha") String title,
		@Schema(description = "Date de la soutenance", example = "2025-06-15") String date,
		@Schema(description = "Heure de la soutenance", example = "09:00") String time,
		@Schema(description = "ID du projet", example = "1") Long projectId,
		@Schema(description = "ID de la salle", example = "1") Long roomId,
		@Schema(description = "Nom de la salle", example = "Salle A101") String roomName,
		@Schema(description = "Titre du projet", example = "Projet IA") String projectTitle,
		@Schema(description = "Noms des étudiants", example = "[\"Alice Martin\", \"Bob Durand\"]") List<String> studentNames,
		@Schema(description = "Rôle dans la soutenance", example = "Rapporteur") String role,
		@Schema(description = "Statut de la soutenance", example = "SCHEDULED") String status) {
}
