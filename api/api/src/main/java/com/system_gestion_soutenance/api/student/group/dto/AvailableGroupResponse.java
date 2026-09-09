package com.system_gestion_soutenance.api.student.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse contenant les informations d'un groupe disponible pour rejoindre")
@SuppressWarnings("PMD")
public record AvailableGroupResponse(@Schema(description = "Identifiant unique du groupe", example = "1") Long id,
		@Schema(description = "Nom du groupe", example = "Groupe Alpha") String groupName,
		@Schema(description = "Nombre de membres actuel dans le groupe", example = "2") int memberCount) {
}
