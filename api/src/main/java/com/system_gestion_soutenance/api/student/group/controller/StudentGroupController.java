package com.system_gestion_soutenance.api.student.group.controller;

import com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse;
import com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse;
import com.system_gestion_soutenance.api.student.group.service.StudentGroupService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/group")
@Tag(name = "Student - Group", description = "Gestion du groupe de soutenance")
public class StudentGroupController {

	private final StudentGroupService studentGroupService;

	public StudentGroupController(StudentGroupService studentGroupService) {
		this.studentGroupService = studentGroupService;
	}

	@GetMapping
	@Operation(summary = "Get the connected student's group workspace")
	public StudentGroupWorkspaceResponse getWorkspace() {
		return studentGroupService.getWorkspace(getCurrentUserId());
	}

	@PostMapping
	@Operation(summary = "Create a new group (during creation period)")
	public ResponseEntity<GroupDetailsResponse> createGroup() {
		GroupDetailsResponse group = studentGroupService.createGroup(getCurrentUserId());
		return ResponseEntity.status(HttpStatus.CREATED).body(group);
	}

	@PostMapping("/{id}/join")
	@Operation(summary = "Join an existing group by ID")
	public GroupDetailsResponse joinGroup(@PathVariable Long id) {
		return studentGroupService.joinGroup(id, getCurrentUserId());
	}

	private Long getCurrentUserId() {
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
	}
}
