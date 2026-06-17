package com.system_gestion_soutenance.api.admin.config.level.controller;

import com.system_gestion_soutenance.api.admin.config.level.dto.CreateLevelRequest;
import com.system_gestion_soutenance.api.admin.config.level.dto.LevelDto;
import com.system_gestion_soutenance.api.admin.config.level.dto.UpdateLevelRequest;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.service.LevelConfigService;
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
@RequestMapping("/api/admin/config/levels")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Level Configuration", description = "Endpoints for managing academic levels")
public class LevelConfigController {

	private final LevelConfigService levelConfigService;
	private final ConfigMapper configMapper;

	public LevelConfigController(LevelConfigService levelConfigService, ConfigMapper configMapper) {
		this.levelConfigService = levelConfigService;
		this.configMapper = configMapper;
	}

	@GetMapping
	@Operation(summary = "List levels", description = "Retrieves all configured academic levels.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved levels")})
	public ApiResponse<PaginatedResponse<LevelDto>> findAll(
			@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
			@Parameter(description = "Items per page (1-500)") @RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		PaginatedResponse<Level> result = levelConfigService.findAll(page, limit);
		List<LevelDto> items = result.items().stream().map(configMapper::toLevelDto).toList();
		PaginatedResponse<LevelDto> mapped = new PaginatedResponse<>(items, result.total(), result.pageCount(),
				result.currentPage(), result.size());
		return ApiResponse.success("Liste des niveaux récupérée avec succès", mapped);
	}

	@PostMapping
	@Operation(summary = "Create level", description = "Creates a new academic level.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Level created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid level data")})
	public ResponseEntity<ApiResponse<LevelDto>> create(@Valid @RequestBody CreateLevelRequest request) {
		Level level = levelConfigService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Niveau créé avec succès", configMapper.toLevelDto(level)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update level", description = "Updates an existing level's details.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Level updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Level not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<LevelDto> update(@Parameter(description = "Level ID") @PathVariable Long id,
			@Valid @RequestBody CreateLevelRequest request) {
		return ApiResponse.success("Niveau mis à jour avec succès",
				configMapper.toLevelDto(levelConfigService.update(id, request)));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update level", description = "Updates only the provided fields of a level.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Level partially updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Level not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<LevelDto> patch(@Parameter(description = "Level ID") @PathVariable Long id,
			@Valid @RequestBody UpdateLevelRequest request) {
		return ApiResponse.success("Niveau mis à jour avec succès",
				configMapper.toLevelDto(levelConfigService.updatePartial(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete level", description = "Removes a level from the system.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Level deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Level not found")})
	public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "Level ID") @PathVariable Long id) {
		levelConfigService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Niveau supprimé avec succès", null));
	}
}
