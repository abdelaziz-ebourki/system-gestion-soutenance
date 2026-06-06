package com.system_gestion_soutenance.api.admin.department.controller;

import com.system_gestion_soutenance.api.admin.department.dto.CreateDepartmentRequest;
import com.system_gestion_soutenance.api.admin.department.dto.DepartmentResponse;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.service.DepartmentService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/departments")
@Tag(name = "Admin - Departments", description = "Gestion des départements")
public class DepartmentController {

	private final DepartmentService departmentService;
	private final ConfigMapper configMapper;

	public DepartmentController(DepartmentService departmentService, ConfigMapper configMapper) {
		this.departmentService = departmentService;
		this.configMapper = configMapper;
	}

	@GetMapping
	@Operation(summary = "List all departments")
	public ApiResponse<List<DepartmentResponse>> findAll() {
		List<DepartmentResponse> departments = departmentService.findAll().stream()
				.map(configMapper::toDepartmentResponse).toList();
		return ApiResponse.success("Liste des départements récupérée avec succès", departments);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a department by ID")
	public ApiResponse<DepartmentResponse> findById(@PathVariable Long id) {
		return ApiResponse.success("Département récupéré avec succès",
				configMapper.toDepartmentResponse(departmentService.findById(id)));
	}

	@PostMapping
	@Operation(summary = "Create a new department")
	public ResponseEntity<ApiResponse<DepartmentResponse>> create(@Valid @RequestBody CreateDepartmentRequest request) {
		Department department = departmentService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(
				ApiResponse.success("Département créé avec succès", configMapper.toDepartmentResponse(department)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a department")
	public ApiResponse<DepartmentResponse> update(@PathVariable Long id,
			@Valid @RequestBody CreateDepartmentRequest request) {
		return ApiResponse.success("Département mis à jour avec succès",
				configMapper.toDepartmentResponse(departmentService.update(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a department")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		departmentService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Département supprimé avec succès", null));
	}
}
