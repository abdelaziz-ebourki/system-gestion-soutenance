package com.system_gestion_soutenance.api.admin.config.grade.controller;

import com.system_gestion_soutenance.api.admin.config.grade.dto.CreateGradeRequest;
import com.system_gestion_soutenance.api.admin.config.grade.dto.GradeDto;
import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import com.system_gestion_soutenance.api.admin.config.grade.service.GradeConfigService;
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
@RequestMapping("/api/admin/config/grades")
@Tag(name = "Admin - Grades", description = "Grade Management")
public class GradeConfigController {

	private final GradeConfigService gradeConfigService;
	private final ConfigMapper configMapper;

	public GradeConfigController(GradeConfigService gradeConfigService, ConfigMapper configMapper) {
		this.gradeConfigService = gradeConfigService;
		this.configMapper = configMapper;
	}

	@GetMapping
	@Operation(summary = "List all grades")
	public ApiResponse<List<GradeDto>> findAll() {
		List<GradeDto> grades = gradeConfigService.findAll().stream().map(configMapper::toGradeDto).toList();
		return ApiResponse.success("Liste des grades récupérée avec succès", grades);
	}

	@PostMapping
	@Operation(summary = "Create a new grade")
	public ResponseEntity<ApiResponse<GradeDto>> create(@Valid @RequestBody CreateGradeRequest request) {
		Grade grade = gradeConfigService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Grade créé avec succès", configMapper.toGradeDto(grade)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a grade")
	public ApiResponse<GradeDto> update(@PathVariable Long id, @Valid @RequestBody CreateGradeRequest request) {
		return ApiResponse.success("Grade mis à jour avec succès",
				configMapper.toGradeDto(gradeConfigService.update(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a grade")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		gradeConfigService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Grade supprimé avec succès", null));
	}
}
