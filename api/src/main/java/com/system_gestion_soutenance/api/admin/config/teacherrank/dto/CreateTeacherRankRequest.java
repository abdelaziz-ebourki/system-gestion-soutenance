package com.system_gestion_soutenance.api.admin.config.teacherrank.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTeacherRankRequest(@NotBlank String name) {
}
