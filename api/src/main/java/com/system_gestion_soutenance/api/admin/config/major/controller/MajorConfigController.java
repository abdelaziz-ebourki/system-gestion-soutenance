package com.system_gestion_soutenance.api.admin.config.major.controller;

import com.system_gestion_soutenance.api.admin.config.major.dto.CreateMajorRequest;
import com.system_gestion_soutenance.api.admin.config.major.dto.MajorDto;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.service.MajorConfigService;
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
	public List<MajorDto> findAll() {
		return majorConfigService.findAll().stream().map(configMapper::toMajorDto).toList();
	}

	@PostMapping
	@Operation(summary = "Create a new major")
	public ResponseEntity<MajorDto> create(@Valid @RequestBody CreateMajorRequest request) {
		Major major = majorConfigService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(configMapper.toMajorDto(major));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a major")
	public MajorDto update(@PathVariable Long id, @Valid @RequestBody CreateMajorRequest request) {
		return configMapper.toMajorDto(majorConfigService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a major")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		majorConfigService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
