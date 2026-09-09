package com.system_gestion_soutenance.api.coordinator.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse détaillée du groupe")
@SuppressWarnings("PMD")
public record GroupResponse(@Schema(description = "Identifiant du groupe", example = "1") Long id,
		@Schema(description = "Nom du groupe", example = "Groupe Alpha") String groupName,
		@Schema(description = "ID du projet associé", example = "1") Long projectId,
		@Schema(description = "Nombre de membres", example = "3") int memberCount,
		@Schema(description = "Noms des étudiants membres", example = "[\"Alice Martin\", \"Bob Durand\"]") List<String> studentNames) {
}
