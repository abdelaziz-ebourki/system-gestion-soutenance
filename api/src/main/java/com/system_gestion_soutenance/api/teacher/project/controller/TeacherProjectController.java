package com.system_gestion_soutenance.api.teacher.project.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.ProjectMapper;
import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectResponse;
import com.system_gestion_soutenance.api.coordinator.project.dto.TeacherProposeProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/teacher/projects")
@Tag(name = "Teacher - Project Proposals", description = "Endpoints for teachers to propose projects")
public class TeacherProjectController {

	private final ProjectService projectService;
	private final ProjectMapper projectMapper;

	public TeacherProjectController(ProjectService projectService, ProjectMapper projectMapper) {
		this.projectService = projectService;
		this.projectMapper = projectMapper;
	}

	@PostMapping
	@PreAuthorize("hasRole('TEACHER')")
	@Operation(summary = "Propose a project", description = "Allows a teacher to propose a new project for students.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Project proposed successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid project data")})
	public ResponseEntity<ApiResponse<ProjectResponse>> propose(
			@Valid @RequestBody TeacherProposeProjectRequest request) {
		Project project = projectService.proposeByTeacher(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Proposition de projet soumise avec succès",
						projectMapper.toDto(project, Collections.emptyMap(), Collections.emptyMap())));
	}
}
