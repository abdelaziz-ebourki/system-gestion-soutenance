package com.system_gestion_soutenance.api.admin.config.grade.controller;

import com.system_gestion_soutenance.api.admin.config.grade.dto.CreateGradeRequest;
import com.system_gestion_soutenance.api.admin.config.grade.dto.GradeDto;
import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import com.system_gestion_soutenance.api.admin.config.grade.service.GradeConfigService;
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
@Tag(name = "Admin - Grades", description = "Gestion des grades")
public class GradeConfigController {

	private final GradeConfigService gradeConfigService;
	private final ConfigMapper configMapper;

	public GradeConfigController(GradeConfigService gradeConfigService, ConfigMapper configMapper) {
		this.gradeConfigService = gradeConfigService;
		this.configMapper = configMapper;
	}

	@GetMapping
	@Operation(summary = "List all grades")
	public List<GradeDto> findAll() {
		return gradeConfigService.findAll().stream().map(configMapper::toGradeDto).toList();
	}

	@PostMapping
	@Operation(summary = "Create a new grade")
	public ResponseEntity<GradeDto> create(@Valid @RequestBody CreateGradeRequest request) {
		Grade grade = gradeConfigService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(configMapper.toGradeDto(grade));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a grade")
	public GradeDto update(@PathVariable Long id, @Valid @RequestBody CreateGradeRequest request) {
		return configMapper.toGradeDto(gradeConfigService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a grade")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		gradeConfigService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
