package com.system_gestion_soutenance.api.student.defense.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse contenant les détails de la soutenance de l'étudiant")
@SuppressWarnings("PMD")
public record StudentDefenseResponse(
		@Schema(description = "Titre du projet de soutenance", example = "Système de gestion de soutenance") String projectTitle,
		@Schema(description = "Description du projet", example = "Application web pour gérer les soutenances") String projectDescription,
		@Schema(description = "Nom du directeur de projet", example = "Dr. Ahmed Ben Salah") String supervisorName,
		@Schema(description = "Liste des membres du jury") List<JuryMemberResponse> juryMembers,
		@Schema(description = "Date de la soutenance", example = "2026-06-15") String date,
		@Schema(description = "Heure de début de la soutenance", example = "08:00") String startTime,
		@Schema(description = "Heure de fin de la soutenance", example = "10:00") String endTime,
		@Schema(description = "Nom de la salle d'examen", example = "Amphi A") String roomName,
		@Schema(description = "Statut de la soutenance", example = "PLANIFIE") String status,
		@Schema(description = "Lien de la convocation", example = "https://convocation.example.com/abc123") String convocationUrl,
		@Schema(description = "Résultat de la soutenance", example = "ADMISSIBLE") String result) {
}
