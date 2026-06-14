package com.system_gestion_soutenance.api.user.controller;

import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.user.dto.BulkCreateRequest;
import com.system_gestion_soutenance.api.user.dto.CreateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UpdateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin - User Management", description = "Endpoints for administering users of the system")
public class UserAdminController {

	private final UserService userService;
	private final UserMapper userMapper;

	public UserAdminController(UserService userService, UserMapper userMapper) {
		this.userService = userService;
		this.userMapper = userMapper;
	}

	@GetMapping
	@Operation(summary = "List users", description = "Retrieves a paginated list of users. Can be filtered by role or search term.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid query parameters")})
	public PaginatedResponse<UserDto> listUsers(
			@Parameter(description = "Filter by user role") @RequestParam(required = false) String role,
			@Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int limit,
			@Parameter(description = "Search term to filter by name or email") @RequestParam(required = false) String search) {
		var userPage = userService.listUsers(role, page, limit, search);
		var items = userPage.getContent().stream().map(userMapper::toDto).toList();
		return new PaginatedResponse<>(items, userPage.getTotalElements(), userPage.getTotalPages(), page, limit);
	}

	@PostMapping
	@Operation(summary = "Create user", description = "Creates a new user in the system with the specified role and details.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid user data"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already exists")})
	public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
		UserDto user = userMapper.toDto(userService.createUser(request));
		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}

	@PostMapping("/bulk")
	@Operation(summary = "Bulk create users", description = "Creates multiple users of the same role in a single request.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Users created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid bulk data")})
	public ResponseEntity<List<UserDto>> bulkCreate(@Valid @RequestBody BulkCreateRequest request) {
		List<UserDto> users = userService.bulkCreate(request).stream().map(userMapper::toDto).toList();
		return ResponseEntity.status(HttpStatus.CREATED).body(users);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update user", description = "Updates information for an existing user.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public UserDto updateUser(@Parameter(description = "User ID") @PathVariable Long id,
			@Valid @RequestBody UpdateUserRequest request) {
		return userMapper.toDto(userService.updateUser(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete user", description = "Permanently removes a user from the system.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "User deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")})
	public ResponseEntity<Void> deleteUser(@Parameter(description = "User ID") @PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}
}
