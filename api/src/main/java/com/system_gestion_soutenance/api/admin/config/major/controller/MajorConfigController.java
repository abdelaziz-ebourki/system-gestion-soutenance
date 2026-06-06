package com.system_gestion_soutenance.api.admin.config.major.controller;

import com.system_gestion_soutenance.api.admin.config.major.dto.CreateMajorRequest;
import com.system_gestion_soutenance.api.admin.config.major.dto.MajorDto;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.service.MajorConfigService;
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
@RequestMapping("/api/admin/config/majors")
@Tag(name = "Admin - Majors", description = "Gestion des filières")
public class MajorConfigController {

	private final MajorConfigService majorConfigService;
	private final ConfigMapper configMapper;

	public MajorConfigController(MajorConfigService majorConfigService, ConfigMapper configMapper) {
		this.majorConfigService = majorConfigService;
		this.configMapper = configMapper;
	}

	@GetMapping
	@Operation(summary = "List all majors")
	public ApiResponse<List<MajorDto>> findAll() {
		List<MajorDto> majors = majorConfigService.findAll().stream().map(configMapper::toMajorDto).toList();
		return ApiResponse.success("Liste des filières récupérée avec succès", majors);
	}

	@PostMapping
	@Operation(summary = "Create a new major")
	public ResponseEntity<ApiResponse<MajorDto>> create(@Valid @RequestBody CreateMajorRequest request) {
		Major major = majorConfigService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Filière créée avec succès", configMapper.toMajorDto(major)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a major")
	public ApiResponse<MajorDto> update(@PathVariable Long id, @Valid @RequestBody CreateMajorRequest request) {
		return ApiResponse.success("Filière mise à jour avec succès",
				configMapper.toMajorDto(majorConfigService.update(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a major")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		majorConfigService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Filière supprimée avec succès", null));
	}
}
