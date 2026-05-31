package com.system_gestion_soutenance.api.teacher.evaluation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record EvaluationSubmitRequest(@DecimalMin("0.0") @DecimalMax("20.0") Double score, String comment) {
}
