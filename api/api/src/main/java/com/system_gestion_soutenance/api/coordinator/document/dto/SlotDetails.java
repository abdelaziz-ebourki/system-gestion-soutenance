package com.system_gestion_soutenance.api.coordinator.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Détails d'un créneau de soutenance")
public record SlotDetails(@Schema(description = "Date de la soutenance", example = "2025-06-15") String date,
		@Schema(description = "Heure de la soutenance", example = "09:00") String time,
		@Schema(description = "Nom de la salle", example = "Salle A101") String roomName,
		@Schema(description = "Titre du projet", example = "Projet IA") String projectTitle,
		@Schema(description = "Noms des étudiants", example = "[\"Alice Martin\", \"Bob Durand\"]") List<String> studentNames) {
}
