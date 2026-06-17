package com.system_gestion_soutenance.api.student.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse contenant l'espace de travail de groupe de l'étudiant")
@SuppressWarnings("PMD")
public record StudentGroupWorkspaceResponse(
		@Schema(description = "Groupe actuel de l'étudiant (null si pas de groupe)") GroupDetailsResponse currentGroup,
		@Schema(description = "Liste des groupes disponibles à rejoindre") List<AvailableGroupResponse> availableGroups,
		@Schema(description = "Date d'ouverture de la création de groupes", example = "2026-06-01") String groupCreationStartDate,
		@Schema(description = "Date de fermeture de la création de groupes", example = "2026-06-30") String groupCreationEndDate,
		@Schema(description = "Indique si la création de groupes est ouverte", example = "true") boolean isGroupCreationOpen) {
}
