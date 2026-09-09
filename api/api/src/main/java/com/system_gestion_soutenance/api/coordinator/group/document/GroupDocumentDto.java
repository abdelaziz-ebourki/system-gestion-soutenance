package com.system_gestion_soutenance.api.coordinator.group.document;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "DTO d'un document de groupe")
public record GroupDocumentDto(@Schema(description = "Identifiant unique du document", example = "1") Long id,
		@Schema(description = "Identifiant du groupe", example = "1") Long groupId,
		@Schema(description = "Type du document", example = "REPORT") GroupDocumentType type,
		@Schema(description = "Nom du document", example = "Rapport PFE") String name,
		@Schema(description = "Date limite de soumission", example = "2026-06-30") LocalDate deadline,
		@Schema(description = "Statut du document", example = "missing") String status,
		@Schema(description = "Date et heure de soumission", example = "2026-06-15T10:30:00") LocalDateTime submittedAt,
		@Schema(description = "Chemin du fichier stocké", example = "/documents/rapport.pdf") String filePath) {
}
