package com.system_gestion_soutenance.api.coordinator.jury.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Requête de mise à jour de jury")
public record UpdateJuryRequest(@Schema(description = "ID du projet associé", example = "1") Long projectId,
		@Schema(description = "Membres du jury") List<MemberEntry> members) {

	@Schema(description = "Membre du jury")
	public record MemberEntry(
			@Schema(description = "ID de l'enseignant (optionnel si externe)", example = "1") Long teacherId,
			@Schema(description = "Rôle dans le jury", example = "Rapporteur") String roleName,
			@Schema(description = "Nom complet du membre externe", example = "Dr. Dupont") String externalName,
			@Schema(description = "Institution du membre externe", example = "Université Paris-Saclay") String externalInstitution,
			@Schema(description = "Email du membre externe", example = "dupont@univ-paris-saclay.fr") String externalEmail) {
	}
}
