package com.system_gestion_soutenance.api.admin.config.level.controller;

import com.system_gestion_soutenance.api.admin.config.level.dto.CreateLevelRequest;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.service.LevelConfigService;
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

	public LevelConfigController(LevelConfigService levelConfigService) {
		this.levelConfigService = levelConfigService;
	}

	@GetMapping
	@Operation(summary = "List all levels")
	public List<Level> findAll() {
		return levelConfigService.findAll();
	}

	@PostMapping
	@Operation(summary = "Create a new level")
	public ResponseEntity<Level> create(@Valid @RequestBody CreateLevelRequest request) {
		Level level = levelConfigService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(level);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a level")
	public Level update(@PathVariable Long id, @Valid @RequestBody CreateLevelRequest request) {
		return levelConfigService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a level")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		levelConfigService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
