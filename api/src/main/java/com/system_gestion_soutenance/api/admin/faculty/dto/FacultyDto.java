package com.system_gestion_soutenance.api.admin.faculty.dto;
@SuppressWarnings("PMD")

public record FacultyDto(Long id, String name, String code, Long deanId, String logoUrl) {
}