package com.system_gestion_soutenance.api.coordinator.group.controller;

import com.system_gestion_soutenance.api.coordinator.group.dto.CreateGroupRequest;
import com.system_gestion_soutenance.api.coordinator.group.dto.GroupResponse;
import com.system_gestion_soutenance.api.coordinator.group.dto.UpdateGroupProjectRequest;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.service.GroupService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.GroupMapper;
import com.system_gestion_soutenance.api.user.entity.Student;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator/groups")
@PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
@Tag(name = "Coordinator - Group Management", description = "Endpoints for managing student groups")
public class GroupController {

	private final GroupService groupService;
	private final GroupMapper groupMapper;

	public GroupController(GroupService groupService, GroupMapper groupMapper) {
		this.groupService = groupService;
		this.groupMapper = groupMapper;
	}

	@GetMapping
	@Operation(summary = "List groups", description = "Retrieves all student groups for the current session.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved groups")})
	public ApiResponse<PaginatedResponse<GroupResponse>> findAll(
			@Parameter(description = "Page number") @RequestParam(defaultValue = "0") @Min(0) int page,
			@Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		PaginatedResponse<Group> result = groupService.findAll(page, limit);
		List<GroupResponse> items = result.items().stream().map(groupMapper::toDto).toList();
		PaginatedResponse<GroupResponse> mapped = new PaginatedResponse<>(items, result.total(), result.pageCount(),
				result.currentPage(), result.size());
		return ApiResponse.success("Liste des groupes récupérée avec succès", mapped);
	}

	@PostMapping
	@Operation(summary = "Create group", description = "Creates a new student group.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Group created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid group data")})
	public ResponseEntity<ApiResponse<GroupResponse>> create(@Valid @RequestBody CreateGroupRequest request) {
		Group group = groupService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Groupe créé avec succès", groupMapper.toDto(group)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Update group project", description = "Assign or update the project linked to an existing group.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Group project updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Group or project not found")})
	public ResponseEntity<ApiResponse<GroupResponse>> updateProject(
			@Parameter(description = "Group ID") @PathVariable Long id,
			@Valid @RequestBody UpdateGroupProjectRequest request) {
		Group group = groupService.updateProject(id, request.projectId());
		return ResponseEntity
				.ok(ApiResponse.success("Projet du groupe mis à jour avec succès", groupMapper.toDto(group)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Delete group", description = "Removes a student group from the system.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Group deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Group not found")})
	public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "Group ID") @PathVariable Long id) {
		groupService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Groupe supprimé avec succès", null));
	}

	@DeleteMapping("/{id}/members/{studentId}")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Remove member from group", description = "Removes a student from a group. Reassigns leadership if the removed member was the leader.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Member removed successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Group or student not found")})
	public ResponseEntity<ApiResponse<Void>> removeMember(@Parameter(description = "Group ID") @PathVariable Long id,
			@Parameter(description = "Student ID") @PathVariable Long studentId) {
		groupService.removeMember(id, studentId);
		return ResponseEntity.ok(ApiResponse.success("Membre supprimé avec succès", null));
	}

	@PatchMapping("/{id}/approve")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Approve group", description = "Validates a PENDING group and sets its status to ACTIVE.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Group approved successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Group is not PENDING")})
	public ResponseEntity<ApiResponse<GroupResponse>> approveGroup(
			@Parameter(description = "Group ID") @PathVariable Long id) {
		Group group = groupService.approveGroup(id);
		return ResponseEntity.ok(ApiResponse.success("Groupe approuvé avec succès", groupMapper.toDto(group)));
	}

	@PatchMapping("/{id}/reject")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Reject group", description = "Rejects a PENDING group and removes it.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Group rejected successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Group is not PENDING")})
	public ResponseEntity<ApiResponse<Void>> rejectGroup(@Parameter(description = "Group ID") @PathVariable Long id) {
		groupService.rejectGroup(id);
		return ResponseEntity.ok(ApiResponse.success("Groupe rejeté avec succès", null));
	}

	@PostMapping("/{id}/assign")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Assign student to group", description = "Manually assigns a student to an existing group.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student assigned successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Group is full or student already in a group")})
	public ResponseEntity<ApiResponse<GroupResponse>> assignStudent(
			@Parameter(description = "Group ID") @PathVariable Long id,
			@Parameter(description = "Student ID") @RequestBody @NotNull Long studentId) {
		Group group = groupService.assignStudentToGroup(studentId, id);
		return ResponseEntity.ok(ApiResponse.success("Étudiant assigné avec succès", groupMapper.toDto(group)));
	}

	@PatchMapping("/sessions/{sessionId}/extend-group-formation")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Extend group formation deadline", description = "Extends the group formation end date by N days.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deadline extended successfully")})
	public ResponseEntity<ApiResponse<Void>> extendGroupFormation(
			@Parameter(description = "Session ID") @PathVariable Long sessionId,
			@Parameter(description = "Number of days to extend") @RequestParam @Min(1) int days) {
		groupService.extendGroupFormation(sessionId, days);
		return ResponseEntity.ok(ApiResponse.success("Date de formation prolongée avec succès", null));
	}

	@GetMapping("/sessions/{sessionId}/ungrouped-students")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "List ungrouped students", description = "Lists students not in any ACTIVE group for a session.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved ungrouped students")})
	public ResponseEntity<ApiResponse<List<Long>>> getUngroupedStudents(
			@Parameter(description = "Session ID") @PathVariable Long sessionId) {
		List<Student> ungrouped = groupService.getUngroupedStudents(sessionId);
		List<Long> ids = ungrouped.stream().map(Student::getId).toList();
		return ResponseEntity.ok(ApiResponse.success("Étudiants non groupés récupérés avec succès", ids));
	}
}
