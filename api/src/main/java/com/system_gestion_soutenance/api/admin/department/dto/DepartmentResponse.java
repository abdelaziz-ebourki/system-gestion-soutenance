package com.system_gestion_soutenance.api.admin.department.dto;

public record DepartmentResponse(Long id, String name, String code, Long headId, Long facultyId, String facultyName) {
}
