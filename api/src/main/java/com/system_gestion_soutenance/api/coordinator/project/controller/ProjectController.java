package com.system_gestion_soutenance.api.coordinator.project.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.ProjectMapper;
import com.system_gestion_soutenance.api.coordinator.project.dto.CreateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectResponse;
import com.system_gestion_soutenance.api.coordinator.project.dto.UpdateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coordinator/projects")
@Tag(name = "Coordinator - Projects", description = "Gestion des projets")
public class ProjectController {

	private final ProjectService projectService;
	private final ProjectMapper projectMapper;

	public ProjectController(ProjectService projectService, ProjectMapper projectMapper) {
		this.projectService = projectService;
		this.projectMapper = projectMapper;
	}

	@GetMapping
	@Operation(summary = "Get all projects")
	public ApiResponse<List<ProjectResponse>> findAll() {
		List<Project> projects = projectService.findAll();
		Map<Long, Long> projectGroupIds = projectService.buildProjectGroupIdMap(projects);
		List<ProjectResponse> response = projects.stream().map(p -> projectMapper.toDto(p, projectGroupIds)).toList();
		return ApiResponse.success("Liste des projets récupérée avec succès", response);
	}

	@PostMapping
	@Operation(summary = "Create a new project")
	public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody CreateProjectRequest request) {
		Project project = projectService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(
				ApiResponse.success("Projet créé avec succès", projectMapper.toDto(project, Collections.emptyMap())));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a project's title, description, or defense type")
	public ApiResponse<ProjectResponse> update(@PathVariable Long id,
			@Valid @RequestBody UpdateProjectRequest updates) {
		Project project = projectService.update(id, updates);
		return ApiResponse.success("Projet mis à jour avec succès",
				projectMapper.toDto(project, Collections.emptyMap()));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a project")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		projectService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Projet supprimé avec succès", null));
	}
}
