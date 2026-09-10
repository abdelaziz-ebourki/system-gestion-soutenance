package com.system_gestion_soutenance.api.student.defense.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse contenant les informations d'un membre du jury")
@SuppressWarnings("PMD")
public record JuryMemberResponse(
		@Schema(description = "Nom complet du membre du jury", example = "Dr. Ahmed Ben Salah") String name,
		@Schema(description = "Rôle du membre du jury", example = "PRESIDENT") String role) {
}
