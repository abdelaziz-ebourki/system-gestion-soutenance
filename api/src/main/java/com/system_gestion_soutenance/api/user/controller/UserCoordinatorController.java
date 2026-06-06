package com.system_gestion_soutenance.api.user.controller;

import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coordinator")
@Tag(name = "Coordinator - Users", description = "Gestion des utilisateurs pour le coordinateur")
public class UserCoordinatorController {

	private final UserService userService;
	private final UserMapper userMapper;

	public UserCoordinatorController(UserService userService, UserMapper userMapper) {
		this.userService = userService;
		this.userMapper = userMapper;
	}

	@GetMapping("/teachers")
	@Operation(summary = "List teachers with pagination")
	public PaginatedResponse<UserDto> listTeachers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5000") int limit, @RequestParam(required = false) String search) {
		var userPage = userService.listUsers("teacher", page, limit, search);
		var items = userPage.getContent().stream().map(userMapper::toDto).toList();
		return new PaginatedResponse<>(items, userPage.getTotalElements(), userPage.getTotalPages(), page, limit);
	}

	@GetMapping("/students")
	@Operation(summary = "List students with pagination")
	public PaginatedResponse<UserDto> listStudents(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5000") int limit, @RequestParam(required = false) String search) {
		var userPage = userService.listUsers("student", page, limit, search);
		var items = userPage.getContent().stream().map(userMapper::toDto).toList();
		return new PaginatedResponse<>(items, userPage.getTotalElements(), userPage.getTotalPages(), page, limit);
	}
}
