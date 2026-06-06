package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleResponse;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface ScheduleMapper {

	@Mapping(target = "roomId", source = "slot.room.id")
	@Mapping(target = "roomName", source = "slot.room.name")
	@Mapping(target = "projectTitle", expression = "java(resolveProjectTitle(slot, projectMap))")
	@Mapping(target = "studentNames", expression = "java(resolveStudentNames(slot, studentNamesMap))")
	@Mapping(target = "role", constant = "")
	@Mapping(target = "status", constant = "scheduled")
	ScheduleResponse toDto(SlotAssignment slot, Map<Long, Project> projectMap, Map<Long, List<String>> studentNamesMap);

	default String resolveProjectTitle(SlotAssignment slot, Map<Long, Project> projectMap) {
		if (slot.getProjectId() == null || projectMap == null)
			return "";
		Project project = projectMap.get(slot.getProjectId());
		return project != null ? project.getTitle() : "";
	}

	default List<String> resolveStudentNames(SlotAssignment slot, Map<Long, List<String>> studentNamesMap) {
		if (slot.getProjectId() == null || studentNamesMap == null)
			return List.of();
		return studentNamesMap.getOrDefault(slot.getProjectId(), List.of());
	}
}
