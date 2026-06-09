package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleResponse;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@SuppressWarnings("PMD")

@Mapper(config = CentralMapperConfig.class)
public interface ScheduleMapper {

	@Mapping(target = "roomId", source = "defense.room.id")
	@Mapping(target = "roomName", source = "defense.room.name")
	@Mapping(target = "projectTitle", expression = "java(resolveProjectTitle(defense, projectMap))")
	@Mapping(target = "studentNames", expression = "java(resolveStudentNames(defense, studentNamesMap))")
	@Mapping(target = "role", constant = "")
	@Mapping(target = "status", constant = "scheduled")
	ScheduleResponse toDto(Defense defense, Map<Long, Project> projectMap, Map<Long, List<String>> studentNamesMap);

	default String resolveProjectTitle(Defense defense, Map<Long, Project> projectMap) {
		if (defense.getProject() == null || projectMap == null)
			return "";
		Project project = projectMap.get(defense.getProject().getId());
		return project != null ? project.getTitle() : "";
	}

	default List<String> resolveStudentNames(Defense defense, Map<Long, List<String>> studentNamesMap) {
		if (defense.getProject() == null || studentNamesMap == null)
			return List.of();
		return studentNamesMap.getOrDefault(defense.getProject().getId(), List.of());
	}
}