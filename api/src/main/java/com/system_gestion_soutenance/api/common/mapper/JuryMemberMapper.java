package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.coordinator.jury.dto.JuryResponse;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@SuppressWarnings("PMD")

@Mapper(config = CentralMapperConfig.class)
public interface JuryMemberMapper {

	@Mapping(target = "teacherId", source = "teacher.id")
	@Mapping(target = "teacherName", expression = "java(teacherName(member))")
	JuryResponse.MemberResponse toDto(JuryMember member);

	default String teacherName(JuryMember member) {
		return member.getTeacher() != null
				? member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName()
				: null;
	}
}