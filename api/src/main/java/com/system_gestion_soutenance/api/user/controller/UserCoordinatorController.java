package com.system_gestion_soutenance.api.user.controller;

import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
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

	public UserCoordinatorController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/teachers")
	@Operation(summary = "List teachers with pagination")
	public PaginatedResponse<UserDto> listTeachers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5000") int limit, @RequestParam(required = false) String search) {
		return userService.listUsers("teacher", page, limit, search);
	}

	@GetMapping("/students")
	@Operation(summary = "List students with pagination")
	public PaginatedResponse<UserDto> listStudents(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5000") int limit, @RequestParam(required = false) String search) {
		return userService.listUsers("student", page, limit, search);
	}
}
