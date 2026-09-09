package com.system_gestion_soutenance.api.student.stats.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse contenant les statistiques de l'étudiant")
@SuppressWarnings("PMD")
public record StudentStatsResponse(@Schema(description = "Nombre de documents soumis", example = "5") int documentCount,
		@Schema(description = "Nombre de documents manquants", example = "2") long missingDocuments,
		@Schema(description = "Nombre de membres dans le groupe", example = "3") int groupMembers,
		@Schema(description = "Statut de la soutenance", example = "PLANIFIE") String defenseStatus) {
}
