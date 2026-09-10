package com.system_gestion_soutenance.api.coordinator.jury.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse détaillée du jury")
@SuppressWarnings("PMD")
public record JuryResponse(@Schema(description = "Identifiant du jury", example = "1") Long id,
		@Schema(description = "ID du projet associé", example = "1") Long projectId,
		@Schema(description = "Titre du projet", example = "Projet IA") String projectTitle,
		@Schema(description = "Type de soutenance", example = "PFE") String defenseType,
		@Schema(description = "Membres du jury") List<MemberResponse> members) {

	@Schema(description = "Réponse d'un membre du jury")
	public record MemberResponse(@Schema(description = "Rôle dans le jury", example = "Rapporteur") String roleName,
			@Schema(description = "ID de l'enseignant", example = "1") Long teacherId,
			@Schema(description = "Nom de l'enseignant", example = "Dr. Dupont") String teacherName,
			@Schema(description = "Nom complet du membre externe", example = "Dr. Dupont") String externalName,
			@Schema(description = "Institution du membre externe", example = "Université Paris-Saclay") String externalInstitution,
			@Schema(description = "Email du membre externe", example = "dupont@univ-paris-saclay.fr") String externalEmail) {
	}
}
