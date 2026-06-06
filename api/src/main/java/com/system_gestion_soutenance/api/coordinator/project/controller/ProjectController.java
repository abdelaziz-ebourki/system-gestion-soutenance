package com.system_gestion_soutenance.api.coordinator.project.controller;

import com.system_gestion_soutenance.api.coordinator.project.dto.CreateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectResponse;
import com.system_gestion_soutenance.api.coordinator.project.dto.UpdateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.service.ProjectService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coordinator/projects")
@Tag(name = "Coordinator - Projects", description = "Gestion des projets")
public class ProjectController {

	private final ProjectService projectService;

	public ProjectController(ProjectService projectService) {
		this.projectService = projectService;
	}

	@GetMapping
	@Operation(summary = "List all projects")
	public ApiResponse<List<ProjectResponse>> findAll() {
		return ApiResponse.success("Liste des projets récupérée avec succès", projectService.findAll());
	}

	@PostMapping
	@Operation(summary = "Create a new project")
	public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody CreateProjectRequest request) {
		ProjectResponse project = projectService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Projet créé avec succès", project));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a project")
	public ApiResponse<ProjectResponse> update(@PathVariable Long id,
			@Valid @RequestBody UpdateProjectRequest updates) {
		return ApiResponse.success("Projet mis à jour avec succès", projectService.update(id, updates));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a project")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		projectService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Projet supprimé avec succès", null));
	}
}
