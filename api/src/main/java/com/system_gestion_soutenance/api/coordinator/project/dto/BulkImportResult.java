package com.system_gestion_soutenance.api.coordinator.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Résultat de l'import massive de projets")
@SuppressWarnings("PMD")
public record BulkImportResult(@Schema(description = "Nombre total de projets", example = "10") int total,
		@Schema(description = "Nombre de projets importés avec succès", example = "8") int imported,
		@Schema(description = "Liste des projets créés") List<BulkProjectResponse> created,
		@Schema(description = "Liste des erreurs survenues lors de l'import") List<BulkImportError> errors) {

	@Schema(description = "Erreur survenue lors de l'import massive")
	public record BulkImportError(@Schema(description = "Numéro de ligne en erreur", example = "3") int line,
			@Schema(description = "Message d'erreur", example = "Titre manquant") String message) {
	}
}
