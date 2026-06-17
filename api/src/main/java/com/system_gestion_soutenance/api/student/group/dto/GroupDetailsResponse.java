package com.system_gestion_soutenance.api.student.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse contenant les détails complets d'un groupe d'étudiants")
@SuppressWarnings("PMD")
public record GroupDetailsResponse(@Schema(description = "Identifiant unique du groupe", example = "1") Long id,
		@Schema(description = "Nom du groupe", example = "Groupe Alpha") String groupName,
		@Schema(description = "Titre du projet du groupe", example = "Système de gestion de soutenance") String projectTitle,
		@Schema(description = "Nom du directeur de projet", example = "Dr. Ahmed Ben Salah") String supervisorName,
		@Schema(description = "Liste des membres du groupe") List<GroupMemberResponse> members) {
}
