package com.system_gestion_soutenance.api.admin.faculty.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFacultyRequest(@NotBlank String name, @NotBlank String code, Long deanId, String logoUrl) {
}
