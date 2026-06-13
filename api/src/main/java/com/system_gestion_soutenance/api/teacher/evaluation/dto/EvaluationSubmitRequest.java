package com.system_gestion_soutenance.api.teacher.evaluation.dto;

import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationAttendanceStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record EvaluationSubmitRequest(@DecimalMin("0.0") @DecimalMax("20.0") Double score, String comment,
		EvaluationAttendanceStatus attendanceStatus) {
}
