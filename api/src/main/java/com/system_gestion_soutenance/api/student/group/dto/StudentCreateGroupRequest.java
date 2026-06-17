package com.system_gestion_soutenance.api.student.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête de création de groupe par un étudiant")
public record StudentCreateGroupRequest(
		@Schema(description = "Nom du groupe", example = "Groupe Alpha") @NotBlank(message = "Le nom du groupe est obligatoire") String groupName,
		@Schema(description = "ID de la session de soutenance", example = "1") @NotNull(message = "La session est obligatoire") Long sessionId) {
}
