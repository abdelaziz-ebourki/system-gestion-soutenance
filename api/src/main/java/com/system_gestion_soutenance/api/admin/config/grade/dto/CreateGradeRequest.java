package com.system_gestion_soutenance.api.admin.config.grade.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGradeRequest(@NotBlank String name) {
}
