package com.system_gestion_soutenance.api.user.controller;

import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - User Shortcuts", description = "Convenience endpoints for listing users by role")
public class AdminUserController {

	private final UserService userService;
	private final UserMapper userMapper;

	public AdminUserController(UserService userService, UserMapper userMapper) {
		this.userService = userService;
		this.userMapper = userMapper;
	}

	@GetMapping("/students")
	@Operation(summary = "List students", description = "Convenience alias for GET /api/admin/users?role=STUDENT")
	public PaginatedResponse<UserDto> listStudents(
			@Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Page size") @RequestParam(defaultValue = "10") int limit,
			@Parameter(description = "Search term") @RequestParam(required = false) String search) {
		var userPage = userService.listUsers("STUDENT", page, limit, search);
		var items = userPage.getContent().stream().map(userMapper::toDto).toList();
		return new PaginatedResponse<>(items, userPage.getTotalElements(), userPage.getTotalPages(), page, limit);
	}

	@GetMapping("/teachers")
	@Operation(summary = "List teachers", description = "Convenience alias for GET /api/admin/users?role=TEACHER")
	public PaginatedResponse<UserDto> listTeachers(
			@Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Page size") @RequestParam(defaultValue = "10") int limit,
			@Parameter(description = "Search term") @RequestParam(required = false) String search) {
		var userPage = userService.listUsers("TEACHER", page, limit, search);
		var items = userPage.getContent().stream().map(userMapper::toDto).toList();
		return new PaginatedResponse<>(items, userPage.getTotalElements(), userPage.getTotalPages(), page, limit);
	}

	@GetMapping("/coordinators")
	@Operation(summary = "List coordinators", description = "Convenience alias for GET /api/admin/users?role=COORDINATOR")
	public PaginatedResponse<UserDto> listCoordinators(
			@Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Page size") @RequestParam(defaultValue = "10") int limit,
			@Parameter(description = "Search term") @RequestParam(required = false) String search) {
		var userPage = userService.listUsers("COORDINATOR", page, limit, search);
		var items = userPage.getContent().stream().map(userMapper::toDto).toList();
		return new PaginatedResponse<>(items, userPage.getTotalElements(), userPage.getTotalPages(), page, limit);
	}

	@GetMapping("/users/teachers-list")
	@Operation(summary = "List all teachers (simple)", description = "Returns all teachers without pagination")
	public java.util.List<UserDto> listAllTeachers() {
		var userPage = userService.listUsers("TEACHER", 0, 1000, null);
		return userPage.getContent().stream().map(userMapper::toDto).toList();
	}

	@GetMapping("/users/students-list")
	@Operation(summary = "List all students (simple)", description = "Returns all students without pagination")
	public java.util.List<UserDto> listAllStudents() {
		var userPage = userService.listUsers("STUDENT", 0, 1000, null);
		return userPage.getContent().stream().map(userMapper::toDto).toList();
	}
}
