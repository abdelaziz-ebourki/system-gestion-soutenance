package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.dto.DepartmentResponse;
import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.TeacherRankDto;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.dto.LevelDto;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.dto.MajorDto;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.faculty.dto.FacultyDto;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.dto.JuryRoleTemplateDto;
import com.system_gestion_soutenance.api.admin.config.juryrole.dto.TemplateRoleDto;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.TemplateRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface ConfigMapper {

	@Mapping(target = "facultyId", source = "faculty.id")
	@Mapping(target = "facultyName", source = "faculty.name")
	DepartmentResponse toDepartmentResponse(Department department);

	@Mapping(target = "deanId", source = "dean.id")
	FacultyDto toFacultyDto(Faculty faculty);

	@Mapping(target = "departmentId", source = "department.id")
	@Mapping(target = "departmentName", source = "department.name")
	MajorDto toMajorDto(Major major);

	TeacherRankDto toTeacherRankDto(TeacherRank teacherRank);

	LevelDto toLevelDto(Level level);

	JuryRoleTemplateDto toJuryRoleTemplateDto(JuryRoleTemplate template);

	TemplateRoleDto toTemplateRoleDto(TemplateRole role);
}
