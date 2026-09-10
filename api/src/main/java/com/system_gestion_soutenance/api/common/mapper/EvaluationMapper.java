package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationResponse;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@SuppressWarnings("PMD")

@Mapper(config = CentralMapperConfig.class)
public interface EvaluationMapper {

	@Mapping(target = "projectTitle", expression = "java(resolveProjectTitle(evaluation, projectMap))")
	@Mapping(target = "finalGrade", source = "evaluation.score")
	@Mapping(target = "status", expression = "java(evaluation.getStatus().name())")
	@Mapping(target = "attendanceStatus", expression = "java(evaluation.getAttendanceStatus() != null ? evaluation.getAttendanceStatus().name() : null)")
	@Mapping(target = "type", expression = "java(evaluation.getType().name())")
	EvaluationResponse toDto(Evaluation evaluation, Map<Long, Project> projectMap);

	default String resolveProjectTitle(Evaluation evaluation, Map<Long, Project> projectMap) {
		Long projectId = evaluation.getDefense() != null && evaluation.getDefense().getProject() != null
				? evaluation.getDefense().getProject().getId()
				: null;
		Project p = projectId != null ? projectMap.get(projectId) : null;
		return p != null ? p.getTitle() : "";
	}
}