package com.system_gestion_soutenance.api.coordinator.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
@SuppressWarnings("PMD")

@Schema(description = "Charge de travail d'un enseignant")
public record TeacherWorkloadResponse(@Schema(description = "ID de l'enseignant") Long teacherId,
		@Schema(description = "Nom de l'enseignant") String teacherName,
		@Schema(description = "Nombre de projets encadrés") int supervisionCount,
		@Schema(description = "Projets encadrés") List<String> supervisedProjects,
		@Schema(description = "Nombre de jurys") int juryCount,
		@Schema(description = "Soutenances comme jury") List<String> juryDefenses,
		@Schema(description = "Charge totale") int totalWorkload) {
}
