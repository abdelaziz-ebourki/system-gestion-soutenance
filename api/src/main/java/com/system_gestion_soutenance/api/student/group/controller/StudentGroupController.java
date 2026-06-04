package com.system_gestion_soutenance.api.student.group.controller;

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

	public StudentGroupController(StudentGroupService studentGroupService, SecurityService securityService) {
		this.studentGroupService = studentGroupService;
		this.securityService = securityService;
	}

	@GetMapping
	@Operation(summary = "Get the connected student's group workspace")
	public StudentGroupWorkspaceResponse getWorkspace() {
		return studentGroupService.getWorkspace(securityService.getCurrentUserId());
	}

	@PostMapping
	@Operation(summary = "Create a new group (during creation period)")
	public ResponseEntity<GroupDetailsResponse> createGroup() {
		GroupDetailsResponse group = studentGroupService.createGroup(securityService.getCurrentUserId());
		return ResponseEntity.status(HttpStatus.CREATED).body(group);
	}

	@PostMapping("/{id}/join")
	@Operation(summary = "Join an existing group by ID")
	public GroupDetailsResponse joinGroup(@PathVariable Long id) {
		return studentGroupService.joinGroup(id, securityService.getCurrentUserId());
	}
}
