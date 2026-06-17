package com.system_gestion_soutenance.api.student.project.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.ProjectMapper;
import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectResponse;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import com.system_gestion_soutenance.api.coordinator.project.service.ProjectService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/student/projects")
@Tag(name = "Student - Project Browsing", description = "Endpoints for students to browse available projects")
public class StudentProjectController {

	private final ProjectService projectService;
	private final ProjectMapper projectMapper;

	public StudentProjectController(ProjectService projectService, ProjectMapper projectMapper) {
		this.projectService = projectService;
		this.projectMapper = projectMapper;
	}

	@GetMapping
	@Operation(summary = "Browse available projects", description = "Retrieves projects available for selection, filtered by status.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved projects")})
	public ApiResponse<List<ProjectResponse>> browse(@AuthenticationPrincipal User user,
			@RequestParam(defaultValue = "PENDING") String status) {
		if (user == null) {
			throw new com.system_gestion_soutenance.api.common.exception.UnauthorizedException(
					"User not authenticated");
		}
		ProjectStatus statusEnum;
		try {
			statusEnum = ProjectStatus.valueOf(status.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Statut invalide: " + status);
		}
		List<Project> projects = projectService.findByStatus(statusEnum);
		Map<Long, Long> projectGroupIds = projectService.buildProjectGroupIdMap(projects);
		List<ProjectResponse> items = projects.stream().map(p -> projectMapper.toDto(p, projectGroupIds)).toList();
		return ApiResponse.success(items);
	}
}
