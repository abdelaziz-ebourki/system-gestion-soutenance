package com.system_gestion_soutenance.api.user.controller;

import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator/users")
@Tag(name = "Coordinator - User Management", description = "Endpoints for coordinators to view users")
public class UserCoordinatorController {

	private final UserService userService;
	private final UserMapper userMapper;

	public UserCoordinatorController(UserService userService, UserMapper userMapper) {
		this.userService = userService;
		this.userMapper = userMapper;
	}

	@GetMapping
	@Operation(summary = "List users", description = "Retrieves a paginated list of users filtered by role (STUDENT or TEACHER).")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid query parameters")})
	public PaginatedResponse<UserDto> listUsers(@RequestParam String role, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5000") int limit, @RequestParam(required = false) String search) {
		var userPage = userService.listUsers(role, page, limit, search);
		var items = userPage.getContent().stream().map(userMapper::toDto).toList();
		return new PaginatedResponse<>(items, userPage.getTotalElements(), userPage.getTotalPages(), page, limit);
	}
}