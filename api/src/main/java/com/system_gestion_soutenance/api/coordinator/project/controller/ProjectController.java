package com.system_gestion_soutenance.api.coordinator.project.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.ProjectMapper;
import com.system_gestion_soutenance.api.coordinator.project.dto.BulkImportResult;
import com.system_gestion_soutenance.api.coordinator.project.dto.BulkProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.dto.CreateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectResponse;
import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectStatusUpdateRequest;
import com.system_gestion_soutenance.api.coordinator.project.dto.UpdateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator/projects")
@PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
@Tag(name = "Coordinator - Project Management", description = "Endpoints for managing student projects")
public class ProjectController {

	private final ProjectService projectService;
	private final ProjectMapper projectMapper;

	private final GroupRepository groupRepository;

	public ProjectController(ProjectService projectService, ProjectMapper projectMapper,
			GroupRepository groupRepository) {
		this.projectService = projectService;
		this.projectMapper = projectMapper;
		this.groupRepository = groupRepository;
	}

	private Map<Long, List<String>> buildProjectStudentNames(List<Project> projects) {
		Map<Long, List<String>> result = new java.util.HashMap<>();
		List<Long> projectIds = projects.stream().map(Project::getId).toList();
		groupRepository.findByProjectIdIn(projectIds).forEach(g -> {
			if (g.getProject() != null && g.getStudents() != null) {
				result.put(g.getProject().getId(),
						g.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName()).toList());
			}
		});
		return result;
	}

	@GetMapping
	@Operation(summary = "List projects", description = "Retrieves all projects assigned for the current session, optionally filtered by status.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved projects")})
	public ApiResponse<PaginatedResponse<ProjectResponse>> findAll(
			@Parameter(description = "Filter by status") @RequestParam(required = false) String status,
			@Parameter(description = "Page number") @RequestParam(defaultValue = "0") @Min(0) int page,
			@Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus statusEnum = status != null
				? com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus
						.valueOf(status.toUpperCase())
				: null;
		PaginatedResponse<Project> result = statusEnum != null
				? projectService.findAllByStatus(statusEnum, page, limit)
				: projectService.findAll(page, limit);
		Map<Long, Long> projectGroupIds = projectService.buildProjectGroupIdMap(result.items());
		Map<Long, List<String>> projectStudentNames = buildProjectStudentNames(result.items());
		List<ProjectResponse> items = result.items().stream()
				.map(p -> projectMapper.toDto(p, projectGroupIds, projectStudentNames)).toList();
		PaginatedResponse<ProjectResponse> mapped = new PaginatedResponse<>(items, result.total(), result.pageCount(),
				result.currentPage(), result.size());
		return ApiResponse.success("Liste des projets récupérée avec succès", mapped);
	}

	@PostMapping
	@Operation(summary = "Create project", description = "Creates a new project.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Project created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid project data")})
	public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody CreateProjectRequest request) {
		Project project = projectService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Projet créé avec succès",
				projectMapper.toDto(project, Collections.emptyMap(), Collections.emptyMap())));
	}

	@PostMapping("/bulk")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Bulk import projects", description = "Imports multiple projects at once. Returns per-row results with errors for failed entries.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Import completed with partial results"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data")})
	public ResponseEntity<ApiResponse<BulkImportResult>> bulkImport(@Valid @RequestBody BulkProjectRequest request) {
		BulkImportResult result = projectService.bulkImport(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Import en masse terminé", result));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Update project", description = "Updates a project's details by its ID.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<ProjectResponse> update(@Parameter(description = "Project ID") @PathVariable Long id,
			@Valid @RequestBody UpdateProjectRequest updates) {
		Project project = projectService.update(id, updates);
		return ApiResponse.success("Projet mis à jour avec succès",
				projectMapper.toDto(project, Collections.emptyMap(), Collections.emptyMap()));
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Update project status", description = "Approves, rejects, or resets a project's status.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status transition")})
	public ApiResponse<ProjectResponse> updateStatus(@Parameter(description = "Project ID") @PathVariable Long id,
			@Valid @RequestBody ProjectStatusUpdateRequest request) {
		Project project = projectService.updateStatus(id, request.status());
		return ApiResponse.success("Statut du projet mis à jour",
				projectMapper.toDto(project, Collections.emptyMap(), Collections.emptyMap()));
	}

	@PatchMapping("/{id}/validate")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Validate proposal", description = "Validates a teacher-proposed project, moving it from PROPOSED to PENDING.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project validated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")})
	public ApiResponse<ProjectResponse> validateProposal(@Parameter(description = "Project ID") @PathVariable Long id) {
		Project project = projectService.updateStatus(id,
				com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus.PENDING);
		return ApiResponse.success("Proposition validée avec succès",
				projectMapper.toDto(project, Collections.emptyMap(), Collections.emptyMap()));
	}

	@PatchMapping("/{id}/reject-proposal")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Reject proposal", description = "Rejects a teacher-proposed project, moving it from PROPOSED to REJECTED.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Proposal rejected successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")})
	public ApiResponse<ProjectResponse> rejectProposal(@Parameter(description = "Project ID") @PathVariable Long id) {
		Project project = projectService.updateStatus(id,
				com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus.REJECTED);
		return ApiResponse.success("Proposition rejetée", projectMapper.toDto(project, Collections.emptyMap(), Collections.emptyMap()));
	}

	@PatchMapping("/{id}/confirm-supervision")
	@PreAuthorize("hasAnyRole('TEACHER', 'COORDINATOR')")
	@Operation(summary = "Confirm supervision", description = "Supervisor confirms their supervision of the project.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supervision confirmed"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")})
	public ApiResponse<ProjectResponse> confirmSupervision(
			@Parameter(description = "Project ID") @PathVariable Long id) {
		Project project = projectService.confirmSupervision(id);
		return ApiResponse.success("Encadrement confirmé", projectMapper.toDto(project, Collections.emptyMap(), Collections.emptyMap()));
	}

	@PatchMapping("/{id}/decline-supervision")
	@PreAuthorize("hasAnyRole('TEACHER', 'COORDINATOR')")
	@Operation(summary = "Decline supervision", description = "Supervisor declines their supervision of the project.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supervision declined"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")})
	public ApiResponse<ProjectResponse> declineSupervision(
			@Parameter(description = "Project ID") @PathVariable Long id) {
		Project project = projectService.declineSupervision(id);
		return ApiResponse.success("Encadrement décliné", projectMapper.toDto(project, Collections.emptyMap(), Collections.emptyMap()));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Delete project", description = "Removes a project from the system.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")})
	public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "Project ID") @PathVariable Long id) {
		projectService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Projet supprimé avec succès", null));
	}
}
