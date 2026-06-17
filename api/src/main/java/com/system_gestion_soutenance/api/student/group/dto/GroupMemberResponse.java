package com.system_gestion_soutenance.api.student.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse contenant les informations d'un membre du groupe")
@SuppressWarnings("PMD")
public record GroupMemberResponse(@Schema(description = "Identifiant unique de l'étudiant", example = "1") Long id,
		@Schema(description = "Nom complet de l'étudiant", example = "Ahmed Ben Ali") String fullName,
		@Schema(description = "Adresse email de l'étudiant", example = "ahmed.benali@etudiant.rnu.tn") String email,
		@Schema(description = "Rôle de l'étudiant dans le groupe", example = "CHEF_DE_GROUPE") String role) {
}
