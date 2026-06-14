package com.system_gestion_soutenance.api.admin.department.dto;

@SuppressWarnings("PMD")

public record UpdateDepartmentRequest(String name, String code, Long headId, Long facultyId) {
}
