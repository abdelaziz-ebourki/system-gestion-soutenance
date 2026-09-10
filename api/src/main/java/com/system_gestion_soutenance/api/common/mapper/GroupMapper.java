package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.coordinator.group.dto.GroupResponse;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@SuppressWarnings("PMD")

@Mapper(config = CentralMapperConfig.class)
public interface GroupMapper {

	@Mapping(target = "projectId", source = "project.id")
	@Mapping(target = "memberCount", expression = "java(memberCount(group))")
	@Mapping(target = "studentNames", expression = "java(studentNames(group))")
	GroupResponse toDto(Group group);

	default int memberCount(Group group) {
		return group.getStudents() != null ? group.getStudents().size() : 0;
	}

	default List<String> studentNames(Group group) {
		if (group.getStudents() == null)
			return List.of();
		return group.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName()).toList();
	}
}