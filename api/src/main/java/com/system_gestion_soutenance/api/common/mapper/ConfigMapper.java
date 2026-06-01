package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.dto.DepartmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface ConfigMapper {

	@Mapping(target = "facultyId", source = "faculty.id")
	@Mapping(target = "facultyName", source = "faculty.name")
	DepartmentResponse toDepartmentResponse(Department department);

	// Since Major, Level, and Grade are returned as entities currently,
	// we can add methods here for when we introduce Response DTOs.
}
