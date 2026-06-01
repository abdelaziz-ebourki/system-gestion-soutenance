package com.system_gestion_soutenance.api.admin.config.level.controller;

import com.system_gestion_soutenance.api.admin.config.level.dto.CreateLevelRequest;
import com.system_gestion_soutenance.api.admin.config.level.dto.LevelDto;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.service.LevelConfigService;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/config/levels")
@Tag(name = "Admin - Levels", description = "Gestion des niveaux")
public class LevelConfigController {

	private final LevelConfigService levelConfigService;
	private final ConfigMapper configMapper;

	public LevelConfigController(LevelConfigService levelConfigService, ConfigMapper configMapper) {
		this.levelConfigService = levelConfigService;
		this.configMapper = configMapper;
	}

	@GetMapping
	@Operation(summary = "List all levels")
	public List<LevelDto> findAll() {
		return levelConfigService.findAll().stream().map(configMapper::toLevelDto).toList();
	}

	@PostMapping
	@Operation(summary = "Create a new level")
	public ResponseEntity<LevelDto> create(@Valid @RequestBody CreateLevelRequest request) {
		Level level = levelConfigService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(configMapper.toLevelDto(level));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a level")
	public LevelDto update(@PathVariable Long id, @Valid @RequestBody CreateLevelRequest request) {
		return configMapper.toLevelDto(levelConfigService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a level")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		levelConfigService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
