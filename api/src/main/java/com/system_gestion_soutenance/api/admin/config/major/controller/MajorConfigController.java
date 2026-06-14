package com.system_gestion_soutenance.api.admin.config.major.controller;

import com.system_gestion_soutenance.api.admin.config.major.dto.CreateMajorRequest;
import com.system_gestion_soutenance.api.admin.config.major.dto.MajorDto;
import com.system_gestion_soutenance.api.admin.config.major.dto.UpdateMajorRequest;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.service.MajorConfigService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/admin/config/majors")
@Tag(name = "Admin - Major Configuration", description = "Endpoints for managing academic majors")
public class MajorConfigController {

	private final MajorConfigService majorConfigService;
	private final ConfigMapper configMapper;

	public MajorConfigController(MajorConfigService majorConfigService, ConfigMapper configMapper) {
		this.majorConfigService = majorConfigService;
		this.configMapper = configMapper;
	}

	@GetMapping
	@Operation(summary = "List majors", description = "Retrieves all configured academic majors.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved majors")})
	public ApiResponse<PaginatedResponse<MajorDto>> findAll(@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		PaginatedResponse<MajorDto> result = majorConfigService.findAll(page, limit);
		return ApiResponse.success("Liste des filières récupérée avec succès", result);
	}

	@PostMapping
	@Operation(summary = "Create major", description = "Creates a new academic major.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Major created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid major data")})
	public ResponseEntity<ApiResponse<MajorDto>> create(@Valid @RequestBody CreateMajorRequest request) {
		Major major = majorConfigService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Filière créée avec succès", configMapper.toMajorDto(major)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update major", description = "Updates an existing major's details.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Major updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Major not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<MajorDto> update(@PathVariable Long id, @Valid @RequestBody CreateMajorRequest request) {
		return ApiResponse.success("Filière mise à jour avec succès",
				configMapper.toMajorDto(majorConfigService.update(id, request)));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update major", description = "Updates only the provided fields of a major.")
	public ApiResponse<MajorDto> patch(@PathVariable Long id, @Valid @RequestBody UpdateMajorRequest request) {
		return ApiResponse.success("Filière mise à jour avec succès",
				configMapper.toMajorDto(majorConfigService.updatePartial(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete major", description = "Removes a major from the system.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Major deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Major not found")})
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		majorConfigService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Filière supprimée avec succès", null));
	}
}