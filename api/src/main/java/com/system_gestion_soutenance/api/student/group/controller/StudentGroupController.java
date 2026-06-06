package com.system_gestion_soutenance.api.student.group.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.StudentGroupMapper;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse;
import com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse;
import com.system_gestion_soutenance.api.student.group.service.StudentGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/group")
@Tag(name = "Student - Group", description = "Gestion du groupe de soutenance")
public class StudentGroupController {

	private final StudentGroupService studentGroupService;
	private final SecurityService securityService;
	private final StudentGroupMapper studentGroupMapper;

	public StudentGroupController(StudentGroupService studentGroupService, SecurityService securityService,
			StudentGroupMapper studentGroupMapper) {
		this.studentGroupService = studentGroupService;
		this.securityService = securityService;
		this.studentGroupMapper = studentGroupMapper;
	}

	@GetMapping
	@Operation(summary = "Get the connected student's group workspace")
	public ApiResponse<StudentGroupWorkspaceResponse> getWorkspace() {
		return ApiResponse.success(studentGroupService.getWorkspace(securityService.getCurrentUserId()));
	}

	@PostMapping
	@Operation(summary = "Create a new group (during creation period)")
	public ResponseEntity<ApiResponse<GroupDetailsResponse>> createGroup() {
		Long studentId = securityService.getCurrentUserId();
		GroupDetailsResponse group = studentGroupMapper.toDetails(studentGroupService.createGroup(studentId),
				studentId);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(group));
	}

	@PostMapping("/{id}/join")
	@Operation(summary = "Join an existing group by ID")
	public ApiResponse<GroupDetailsResponse> joinGroup(@PathVariable Long id) {
		Long studentId = securityService.getCurrentUserId();
		return ApiResponse
				.success(studentGroupMapper.toDetails(studentGroupService.joinGroup(id, studentId), studentId));
	}
}
