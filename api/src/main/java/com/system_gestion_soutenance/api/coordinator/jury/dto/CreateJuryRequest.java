package com.system_gestion_soutenance.api.coordinator.jury.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Requête de création de jury")
public record CreateJuryRequest(
		@Schema(description = "ID du projet associé", example = "1") @NotNull(message = "Le projet est obligatoire") Long projectId,
		@Schema(description = "Membres du jury") @NotNull(message = "L'équipe du jury est obligatoire") List<MemberEntry> members) {

	@Schema(description = "Membre du jury")
	public record MemberEntry(
			@Schema(description = "ID de l'enseignant (optionnel si externe)", example = "1") Long teacherId,
			@Schema(description = "Rôle dans le jury", example = "Rapporteur") @NotNull(message = "Le rôle est obligatoire") String roleName,
			@Schema(description = "Nom complet du membre externe", example = "Dr. Dupont") String externalName,
			@Schema(description = "Institution du membre externe", example = "Université Paris-Saclay") String externalInstitution,
			@Schema(description = "Email du membre externe", example = "dupont@univ-paris-saclay.fr") String externalEmail) {
	}
}
