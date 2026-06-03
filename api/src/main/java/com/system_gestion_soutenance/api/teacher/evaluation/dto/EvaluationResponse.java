package com.system_gestion_soutenance.api.teacher.evaluation.dto;

import java.util.List;

public record EvaluationResponse(
    Long id,
    Long projectId,
    String projectTitle,
    Double finalGrade,
    String comment,
    String status
) {}
