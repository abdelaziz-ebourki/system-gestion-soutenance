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

	@Mapping(target = "status", expression = "java(project.getStatus() != null ? project.getStatus().name() : null)")
	@Mapping(target = "groupId", expression = "java(projectGroupIds != null ? projectGroupIds.get(project.getId()) : null)")
	@Mapping(target = "supervisorName", expression = "java(resolveSupervisorName(project))")
	@Mapping(target = "studentNames", expression = "java(projectStudentNames != null ? projectStudentNames.getOrDefault(project.getId(), List.of()) : List.of())")
	@Mapping(target = "coSupervisors", expression = "java(mapCoSupervisors(project))")
	ProjectResponse toDto(Project project, Map<Long, Long> projectGroupIds,
			Map<Long, List<String>> projectStudentNames);

	default String resolveSupervisorName(Project project) {
		if (project.getSupervisor() == null)
			return null;
		return project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName();
	}

	default List<ProjectResponse.CoSupervisorDto> mapCoSupervisors(Project project) {
		if (project.getCoSupervisors() == null)
			return List.of();
		return project.getCoSupervisors().stream().map(t -> new ProjectResponse.CoSupervisorDto(t.getId(),
				t.getFirstName() + " " + t.getLastName(), t.getEmail())).toList();
	}
}