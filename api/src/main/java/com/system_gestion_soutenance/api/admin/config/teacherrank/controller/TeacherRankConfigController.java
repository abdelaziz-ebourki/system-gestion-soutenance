package com.system_gestion_soutenance.api.admin.config.teacherrank.controller;

import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.CreateTeacherRankRequest;
import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.TeacherRankDto;
import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.UpdateTeacherRankRequest;
import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.teacherrank.service.TeacherRankConfigService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/admin/config/teacher-ranks")
@Tag(name = "Admin - Teacher Ranks", description = "Teacher Rank Management")
public class TeacherRankConfigController {

	private final TeacherRankConfigService teacherRankConfigService;
	private final ConfigMapper configMapper;

	public TeacherRankConfigController(TeacherRankConfigService teacherRankConfigService, ConfigMapper configMapper) {
		this.teacherRankConfigService = teacherRankConfigService;
		this.configMapper = configMapper;
	}

	@GetMapping
	@Operation(summary = "List all teacher ranks")
	public ApiResponse<List<TeacherRankDto>> findAll() {
		List<TeacherRankDto> ranks = teacherRankConfigService.findAll().stream().map(configMapper::toTeacherRankDto)
				.toList();
		return ApiResponse.success("Liste des ranks récupérée avec succès", ranks);
	}

	@PostMapping
	@Operation(summary = "Create a new teacher rank")
	public ResponseEntity<ApiResponse<TeacherRankDto>> create(@Valid @RequestBody CreateTeacherRankRequest request) {
		TeacherRank teacherRank = teacherRankConfigService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Rank créé avec succès", configMapper.toTeacherRankDto(teacherRank)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a teacher rank")
	public ApiResponse<TeacherRankDto> update(@PathVariable Long id,
			@Valid @RequestBody CreateTeacherRankRequest request) {
		return ApiResponse.success("Rank mis à jour avec succès",
				configMapper.toTeacherRankDto(teacherRankConfigService.update(id, request)));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update a teacher rank")
	public ApiResponse<TeacherRankDto> patch(@PathVariable Long id,
			@Valid @RequestBody UpdateTeacherRankRequest request) {
		return ApiResponse.success("Rank mis à jour avec succès",
				configMapper.toTeacherRankDto(teacherRankConfigService.updatePartial(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a teacher rank")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		teacherRankConfigService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Rank supprimé avec succès", null));
	}
}
