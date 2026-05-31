package com.system_gestion_soutenance.api.admin.department.dto;

import com.system_gestion_soutenance.api.admin.department.entity.Department;

public record DepartmentResponse(Long id, String name, String code, Long headId, Long facultyId) {
	public static DepartmentResponse from(Department dept) {
		return new DepartmentResponse(dept.getId(), dept.getName(), dept.getCode(), dept.getHeadId(),
				dept.getFacultyId());
	}
}
