package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectResponse;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@SuppressWarnings("PMD")

@Mapper(config = CentralMapperConfig.class)
public interface ProjectMapper {

	@Mapping(target = "groupId", expression = "java(projectGroupIds != null ? projectGroupIds.get(project.getId()) : null)")
	@Mapping(target = "supervisorName", expression = "java(resolveSupervisorName(project))")
	@Mapping(target = "studentNames", expression = "java(resolveStudentNames(project))")
	ProjectResponse toDto(Project project, Map<Long, Long> projectGroupIds);

	default String resolveSupervisorName(Project project) {
		if (project.getSupervisor() == null)
			return null;
		return project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName();
	}

	default List<String> resolveStudentNames(Project project) {
		if (project.getStudents() == null)
			return List.of();
		return project.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName()).toList();
	}
}