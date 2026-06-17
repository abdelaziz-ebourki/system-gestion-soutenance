package com.system_gestion_soutenance.api.coordinator.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse de convocation du jury")
@SuppressWarnings("PMD")
public record JuryConvocationResponse(
		@Schema(description = "Nom de l'enseignant", example = "Dr. Dupont") String teacherName,
		@Schema(description = "Rôle dans le jury", example = "Rapporteur") String role,
		@Schema(description = "Titre du projet", example = "Projet IA") String projectTitle,
		@Schema(description = "Noms des étudiants", example = "[\"Alice Martin\", \"Bob Durand\"]") List<String> studentNames,
		@Schema(description = "Date de la soutenance", example = "2025-06-15") String date,
		@Schema(description = "Heure de la soutenance", example = "09:00") String time,
		@Schema(description = "Nom de la salle", example = "Salle A101") String roomName,
		@Schema(description = "Nom de la session de soutenance", example = "Session Juin 2025") String defenseSessionName) {
}
