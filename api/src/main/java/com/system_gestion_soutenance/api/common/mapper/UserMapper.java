package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.SubclassMapping;
@SuppressWarnings("PMD")

@Mapper(config = CentralMapperConfig.class)
public interface UserMapper {

	@SubclassMapping(source = Student.class, target = UserDto.class)
	@SubclassMapping(source = Teacher.class, target = UserDto.class)
	@Mapping(target = "role", source = "role", qualifiedByName = "roleToLowerCase")
	UserDto toDto(User user);

	@Mapping(target = "role", source = "role", qualifiedByName = "roleToLowerCase")
	@Mapping(target = "cne", source = "cne")
	@Mapping(target = "codeApogee", source = "codeApogee")
	@Mapping(target = "majorId", source = "major.id")
	@Mapping(target = "majorName", source = "major.name")
	@Mapping(target = "levelId", source = "level.id")
	@Mapping(target = "levelName", source = "level.name")
	@Mapping(target = "departmentId", source = "major.department.id")
	@Mapping(target = "departmentName", source = "major.department.name")
	@Mapping(target = "teacherRankId", ignore = true)
	@Mapping(target = "teacherRankName", ignore = true)
	UserDto toDto(Student student);

	@Mapping(target = "role", source = "role", qualifiedByName = "roleToLowerCase")
	@Mapping(target = "cne", ignore = true)
	@Mapping(target = "codeApogee", ignore = true)
	@Mapping(target = "majorId", ignore = true)
	@Mapping(target = "majorName", ignore = true)
	@Mapping(target = "levelId", ignore = true)
	@Mapping(target = "levelName", ignore = true)
	@Mapping(target = "teacherRankId", source = "teacherRank.id")
	@Mapping(target = "teacherRankName", source = "teacherRank.name")
	@Mapping(target = "departmentId", source = "department.id")
	@Mapping(target = "departmentName", source = "department.name")
	UserDto toDto(Teacher teacher);

	@Named("roleToLowerCase")
	default String roleToLowerCase(com.system_gestion_soutenance.api.user.entity.Role role) {
		return role == null ? null : role.name().toLowerCase();
	}
}