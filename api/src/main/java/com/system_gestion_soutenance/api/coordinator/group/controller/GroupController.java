package com.system_gestion_soutenance.api.coordinator.group.controller;

import com.system_gestion_soutenance.api.coordinator.group.dto.CreateGroupRequest;
import com.system_gestion_soutenance.api.coordinator.group.dto.GroupResponse;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.service.GroupService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.GroupMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
	public ApiResponse<List<GroupResponse>> findAll() {
		List<Group> groups = groupService.findAll();
		return ApiResponse.success("Liste des groupes récupérée avec succès",
				groups.stream().map(groupMapper::toDto).toList());
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
	@Operation(summary = "Delete group", description = "Removes a student group from the system.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Group deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Group not found")})
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		groupService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Groupe supprimé avec succès", null));
	}
}
