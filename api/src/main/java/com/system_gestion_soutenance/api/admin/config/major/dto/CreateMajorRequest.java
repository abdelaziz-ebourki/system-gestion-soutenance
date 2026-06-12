package com.system_gestion_soutenance.api.admin.config.major.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMajorRequest(@NotBlank String name, Long departmentId) {
}
