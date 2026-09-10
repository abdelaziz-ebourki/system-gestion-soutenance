package com.system_gestion_soutenance.api.student.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "DTO d'un document de l'étudiant")
@SuppressWarnings("PMD")
public record StudentDocumentDto(@Schema(description = "Identifiant unique du document", example = "1") Long id,
		@Schema(description = "Identifiant de l'étudiant propriétaire", example = "10") Long studentId,
		@Schema(description = "Nom du document", example = "Rapport de stage") String name,
		@Schema(description = "Type du document", example = "PDF") String type,
		@Schema(description = "Date limite de soumission", example = "2026-06-30") LocalDate deadline,
		@Schema(description = "Statut du document", example = "SOUMIS") String status,
		@Schema(description = "Date et heure de soumission", example = "2026-06-15T10:30:00") LocalDateTime submittedAt,
		@Schema(description = "Chemin du fichier stocké", example = "/documents/rapport_stage.pdf") String filePath) {
}
