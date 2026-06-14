package com.system_gestion_soutenance.api.student.group.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.StudentGroupMapper;
import com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse;
import com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse;
import com.system_gestion_soutenance.api.student.group.service.StudentGroupService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/student/groups")
@Tag(name = "Student - Group Management", description = "Endpoints for students to manage their defense groups")
public class StudentGroupController {

	private final StudentGroupService studentGroupService;
	private final StudentGroupMapper studentGroupMapper;

	public StudentGroupController(StudentGroupService studentGroupService, StudentGroupMapper studentGroupMapper) {
		this.studentGroupService = studentGroupService;
		this.studentGroupMapper = studentGroupMapper;
	}

	@GetMapping
	@Operation(summary = "Get group workspace", description = "Retrieves the workspace and group details for the connected student.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved workspace")})
	public ApiResponse<StudentGroupWorkspaceResponse> getWorkspace(@AuthenticationPrincipal User user) {
		return ApiResponse.success(studentGroupService.getWorkspace(user.getId()));
	}

	@PostMapping
	@Operation(summary = "Create group", description = "Creates a new group for the connected student (during creation period).")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Group created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Creation period closed or invalid request")})
	public ResponseEntity<ApiResponse<GroupDetailsResponse>> createGroup(@AuthenticationPrincipal User user) {
		Long studentId = user.getId();
		GroupDetailsResponse group = studentGroupMapper.toDetails(studentGroupService.createGroup(studentId),
				studentId);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(group));
	}

	@PostMapping("/{id}/members")
	@Operation(summary = "Join group", description = "Allows a student to join an existing group by its ID.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully joined the group"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Group is full or student already in a group"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Group not found")})
	public ApiResponse<GroupDetailsResponse> joinGroup(@Parameter(description = "Group ID") @PathVariable Long id,
			@AuthenticationPrincipal User user) {
		Long studentId = user.getId();
		return ApiResponse
				.success(studentGroupMapper.toDetails(studentGroupService.joinGroup(id, studentId), studentId));
	}

	@DeleteMapping("/leave")
	@Operation(summary = "Leave group", description = "Allows a student to leave their current group. Blocked if the group has an assigned project.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully left the group"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Not in a group or group has a project")})
	public ResponseEntity<ApiResponse<Void>> leaveGroup(@AuthenticationPrincipal User user) {
		studentGroupService.leaveGroup(user.getId());
		return ResponseEntity.ok(ApiResponse.success("Vous avez quitté le groupe", null));
	}
}
