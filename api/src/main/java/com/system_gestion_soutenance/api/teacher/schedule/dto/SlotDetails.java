package com.system_gestion_soutenance.api.teacher.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Détails d'un créneau de soutenance")
@SuppressWarnings("PMD")
public record SlotDetails(@Schema(description = "Identifiant unique du créneau", example = "1") Long id,
		@Schema(description = "Identifiant du projet associé", example = "10") Long projectId,
		@Schema(description = "Titre du projet", example = "Système de gestion de soutenance") String projectTitle,
		@Schema(description = "Noms des étudiants du groupe", example = "[\"Ahmed Ben Ali\", \"Sara Trabelsi\"]") List<String> studentNames,
		@Schema(description = "Date de la soutenance", example = "2026-06-15") String date,
		@Schema(description = "Heure de début du créneau", example = "08:00") String startTime,
		@Schema(description = "Heure de fin du créneau", example = "10:00") String endTime,
		@Schema(description = "Nom de la salle d'examen", example = "Amphi A") String roomName,
		@Schema(description = "Rôle de l'enseignant dans ce créneau", example = "JURY") String role,
		@Schema(description = "Statut du créneau", example = "PLANIFIE") String status) {
}
