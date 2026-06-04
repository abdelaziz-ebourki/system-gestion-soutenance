package com.system_gestion_soutenance.api.teacher.evaluation.dto;

public record EvaluationResponse(Long id, Long projectId, String projectTitle, Double finalGrade, String comment,
		String status) {
}
