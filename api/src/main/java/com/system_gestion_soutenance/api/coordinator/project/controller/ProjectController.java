package com.system_gestion_soutenance.api.coordinator.project.controller;

import com.system_gestion_soutenance.api.coordinator.project.dto.CreateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

	public ProjectController(ProjectService projectService) {
		this.projectService = projectService;
	}

	@GetMapping
	@Operation(summary = "List all projects")
	public List<Map<String, Object>> findAll() {
		return projectService.findAll();
	}

	@PostMapping
	@Operation(summary = "Create a new project")
	public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateProjectRequest request) {
		Map<String, Object> project = projectService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(project);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a project")
	public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
		return projectService.update(id, updates);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a project")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		projectService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
