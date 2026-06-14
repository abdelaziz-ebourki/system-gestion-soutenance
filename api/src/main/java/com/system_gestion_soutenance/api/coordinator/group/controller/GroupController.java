package com.system_gestion_soutenance.api.coordinator.group.controller;

import com.system_gestion_soutenance.api.coordinator.group.dto.CreateGroupRequest;
import com.system_gestion_soutenance.api.coordinator.group.dto.GroupResponse;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.service.GroupService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.GroupMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator/groups")
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
}
