package com.system_gestion_soutenance.api.coordinator.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Requête d'import massive de projets")
public record BulkProjectRequest(
		@Schema(description = "Liste des projets à importer") @NotEmpty(message = "La liste des projets ne peut être vide") @Valid List<BulkProjectEntry> projects) {
}
