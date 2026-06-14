package com.system_gestion_soutenance.api.teacher.evaluation.dto;
@SuppressWarnings("PMD")

public record EvaluationResponse(Long id, Long projectId, String projectTitle, Double finalGrade, String comment,
		String status, String attendanceStatus) {
}