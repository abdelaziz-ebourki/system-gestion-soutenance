package com.system_gestion_soutenance.api.admin.department.controller;

import com.system_gestion_soutenance.api.admin.department.dto.CreateDepartmentRequest;
import com.system_gestion_soutenance.api.admin.department.dto.DepartmentResponse;
import com.system_gestion_soutenance.api.admin.department.dto.UpdateDepartmentRequest;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.service.DepartmentService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/admin/departments")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Department Management", description = "Endpoints for managing academic departments")
public class DepartmentController {

	private final DepartmentService departmentService;
	private final ConfigMapper configMapper;

	public DepartmentController(DepartmentService departmentService, ConfigMapper configMapper) {
		this.departmentService = departmentService;
		this.configMapper = configMapper;
	}

	@GetMapping
	@Operation(summary = "List departments", description = "Retrieves all academic departments.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved departments")})
	public ApiResponse<PaginatedResponse<DepartmentResponse>> findAll(
			@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
			@Parameter(description = "Items per page (1-500)") @RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		PaginatedResponse<Department> result = departmentService.findAll(page, limit);
		List<DepartmentResponse> items = result.items().stream().map(configMapper::toDepartmentResponse).toList();
		PaginatedResponse<DepartmentResponse> mapped = new PaginatedResponse<>(items, result.total(),
				result.pageCount(), result.currentPage(), result.size());
		return ApiResponse.success("Liste des départements récupérée avec succès", mapped);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get department", description = "Retrieves details of a specific department by its ID.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved department"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")})
	public ApiResponse<DepartmentResponse> findById(@Parameter(description = "Department ID") @PathVariable Long id) {
		return ApiResponse.success("Département récupéré avec succès",
				configMapper.toDepartmentResponse(departmentService.findById(id)));
	}

	@PostMapping
	@Operation(summary = "Create department", description = "Creates a new academic department.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Department created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid department data")})
	public ResponseEntity<ApiResponse<DepartmentResponse>> create(@Valid @RequestBody CreateDepartmentRequest request) {
		Department department = departmentService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(
				ApiResponse.success("Département créé avec succès", configMapper.toDepartmentResponse(department)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update department", description = "Updates an existing department's details.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<DepartmentResponse> update(@Parameter(description = "Department ID") @PathVariable Long id,
			@Valid @RequestBody CreateDepartmentRequest request) {
		return ApiResponse.success("Département mis à jour avec succès",
				configMapper.toDepartmentResponse(departmentService.update(id, request)));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update department", description = "Updates only the provided fields of a department.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department partially updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<DepartmentResponse> patch(@Parameter(description = "Department ID") @PathVariable Long id,
			@Valid @RequestBody UpdateDepartmentRequest request) {
		return ApiResponse.success("Département mis à jour avec succès",
				configMapper.toDepartmentResponse(departmentService.updatePartial(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete department", description = "Removes a department from the system.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")})
	public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "Department ID") @PathVariable Long id) {
		departmentService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Département supprimé avec succès", null));
	}
}
