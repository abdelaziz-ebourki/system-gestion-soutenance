package com.system_gestion_soutenance.api.coordinator.grade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
@SuppressWarnings("PMD")

@Schema(description = "État de la délibération d'une session")
public record DeliberationStateResponse(@Schema(description = "ID de la session") Long sessionId,
		@Schema(description = "Nom de la session") String sessionName,
		@Schema(description = "Délibéré par (ID utilisateur)") Long deliberatedBy,
		@Schema(description = "Date de délibération") LocalDateTime deliberatedAt,
		@Schema(description = "Validé par (ID administrateur)") Long validatedBy,
		@Schema(description = "Date de validation") LocalDateTime validatedAt,
		@Schema(description = "Résultats publiés") boolean resultsPublished,
		@Schema(description = "Détails des défenses") List<DefenseDeliberationDetails> defenses) {

	@Schema(description = "Détails de délibération d'une défense")
	public record DefenseDeliberationDetails(@Schema(description = "ID du projet") Long projectId,
			@Schema(description = "Titre du projet") String projectTitle,
			@Schema(description = "Note finale") Double finalScore, @Schema(description = "Mention") String mention,
			@Schema(description = "Commentaire") String comment) {
	}
}
