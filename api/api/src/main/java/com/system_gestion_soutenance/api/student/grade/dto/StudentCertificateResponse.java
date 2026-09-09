package com.system_gestion_soutenance.api.student.grade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
@SuppressWarnings("PMD")

@Schema(description = "Données pour le certificat de l'étudiant")
public record StudentCertificateResponse(@Schema(description = "Nom de l'établissement") String institutionName,
		@Schema(description = "Nom de l'étudiant") String studentName,
		@Schema(description = "Titre du projet") String projectTitle,
		@Schema(description = "Type de soutenance") String defenseType,
		@Schema(description = "Date de soutenance") String defenseDate,
		@Schema(description = "Note finale") Double finalScore, @Schema(description = "Mention") String mention,
		@Schema(description = "Noms des membres du jury") List<String> juryMemberNames) {
}
