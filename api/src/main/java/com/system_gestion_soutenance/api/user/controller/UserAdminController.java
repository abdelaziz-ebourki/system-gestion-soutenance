package com.system_gestion_soutenance.api.user.controller;

import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.user.dto.BulkCreateRequest;
import com.system_gestion_soutenance.api.user.dto.CreateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UpdateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin - Users", description = "Gestion des utilisateurs")
public class UserAdminController {

	private final UserService userService;

	public UserAdminController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/users")
	@Operation(summary = "List users with pagination and optional role/search filters")
	public PaginatedResponse<UserDto> listUsers(@RequestParam(required = false) String role,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit,
			@RequestParam(required = false) String search) {
		return userService.listUsers(role, page, limit, search);
	}

	@GetMapping("/users/teachers-list")
	@Operation(summary = "List all teachers (unpaginated)")
	public List<UserDto> listAllTeachers() {
		return userService.listAllByRole("teacher");
	}

	@GetMapping("/users/students-list")
	@Operation(summary = "List all students (unpaginated)")
	public List<UserDto> listAllStudents() {
		return userService.listAllByRole("student");
	}

	@GetMapping("/students")
	@Operation(summary = "List students with pagination")
	public PaginatedResponse<UserDto> listStudents(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit, @RequestParam(required = false) String search) {
		return userService.listUsers("student", page, limit, search);
	}

	@GetMapping("/teachers")
	@Operation(summary = "List teachers with pagination")
	public PaginatedResponse<UserDto> listTeachers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit, @RequestParam(required = false) String search) {
		return userService.listUsers("teacher", page, limit, search);
	}

	@GetMapping("/coordinators")
	@Operation(summary = "List coordinators with pagination")
	public PaginatedResponse<UserDto> listCoordinators(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit, @RequestParam(required = false) String search) {
		return userService.listUsers("coordinator", page, limit, search);
	}

	@PostMapping("/users")
	@Operation(summary = "Create a new user")
	public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
		UserDto user = userService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}

	@PostMapping("/students")
	@Operation(summary = "Create a new student")
	public ResponseEntity<UserDto> createStudent(@Valid @RequestBody CreateUserRequest request) {
		if (request.role() == null || request.role().isBlank()) {
			request = new CreateUserRequest(request.lastName(), request.firstName(), request.email(), "student",
					request.cne(), request.majorId(), request.levelId(), null, null);
		}
		UserDto user = userService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}

	@PostMapping("/teachers")
	@Operation(summary = "Create a new teacher")
	public ResponseEntity<UserDto> createTeacher(@Valid @RequestBody CreateUserRequest request) {
		if (request.role() == null || request.role().isBlank()) {
			request = new CreateUserRequest(request.lastName(), request.firstName(), request.email(), "teacher", null,
					null, null, request.gradeId(), request.departmentId());
		}
		UserDto user = userService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}

	@PostMapping("/coordinators")
	@Operation(summary = "Create a new coordinator")
	public ResponseEntity<UserDto> createCoordinator(@Valid @RequestBody CreateUserRequest request) {
		if (request.role() == null || request.role().isBlank()) {
			request = new CreateUserRequest(request.lastName(), request.firstName(), request.email(), "coordinator",
					null, null, null, null, null);
		}
		UserDto user = userService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}

	@PostMapping("/users/bulk")
	@Operation(summary = "Bulk create users")
	public ResponseEntity<List<UserDto>> bulkCreate(@Valid @RequestBody BulkCreateRequest request) {
		List<UserDto> users = userService.bulkCreate(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(users);
	}

	@PutMapping("/users/{id}")
	@Operation(summary = "Update a user")
	public UserDto updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
		return userService.updateUser(id, request);
	}

	@DeleteMapping("/users/{id}")
	@Operation(summary = "Delete a user")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}
}
