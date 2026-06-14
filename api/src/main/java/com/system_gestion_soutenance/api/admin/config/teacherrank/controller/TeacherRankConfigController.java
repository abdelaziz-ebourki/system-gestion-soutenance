package com.system_gestion_soutenance.api.admin.config.teacherrank.controller;

import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.CreateTeacherRankRequest;
import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.TeacherRankDto;
import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.UpdateTeacherRankRequest;
import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.teacherrank.service.TeacherRankConfigService;
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
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved teacher ranks")})
	public ApiResponse<PaginatedResponse<TeacherRankDto>> findAll(
			@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
			@Parameter(description = "Items per page (1-500)") @RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		PaginatedResponse<TeacherRank> result = teacherRankConfigService.findAll(page, limit);
		List<TeacherRankDto> items = result.items().stream().map(configMapper::toTeacherRankDto).toList();
		PaginatedResponse<TeacherRankDto> mapped = new PaginatedResponse<>(items, result.total(), result.pageCount(),
				result.currentPage(), result.size());
		return ApiResponse.success("Liste des ranks récupérée avec succès", mapped);
	}

	@PostMapping
	@Operation(summary = "Create a new teacher rank")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Teacher rank created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid teacher rank data")})
	public ResponseEntity<ApiResponse<TeacherRankDto>> create(@Valid @RequestBody CreateTeacherRankRequest request) {
		TeacherRank teacherRank = teacherRankConfigService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Rank créé avec succès", configMapper.toTeacherRankDto(teacherRank)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a teacher rank")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teacher rank updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Teacher rank not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<TeacherRankDto> update(@Parameter(description = "Teacher rank ID") @PathVariable Long id,
			@Valid @RequestBody CreateTeacherRankRequest request) {
		return ApiResponse.success("Rank mis à jour avec succès",
				configMapper.toTeacherRankDto(teacherRankConfigService.update(id, request)));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update a teacher rank")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teacher rank partially updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Teacher rank not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<TeacherRankDto> patch(@Parameter(description = "Teacher rank ID") @PathVariable Long id,
			@Valid @RequestBody UpdateTeacherRankRequest request) {
		return ApiResponse.success("Rank mis à jour avec succès",
				configMapper.toTeacherRankDto(teacherRankConfigService.updatePartial(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a teacher rank")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teacher rank deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Teacher rank not found")})
	public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "Teacher rank ID") @PathVariable Long id) {
		teacherRankConfigService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Rank supprimé avec succès", null));
	}
}
