package com.system_gestion_soutenance.api.admin.config.major.controller;

import com.system_gestion_soutenance.api.admin.config.major.dto.CreateMajorRequest;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.service.MajorConfigService;
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

	public MajorConfigController(MajorConfigService majorConfigService) {
		this.majorConfigService = majorConfigService;
	}

	@GetMapping
	@Operation(summary = "List all majors")
	public List<Major> findAll() {
		return majorConfigService.findAll();
	}

	@PostMapping
	@Operation(summary = "Create a new major")
	public ResponseEntity<Major> create(@Valid @RequestBody CreateMajorRequest request) {
		Major major = majorConfigService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(major);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a major")
	public Major update(@PathVariable Long id, @Valid @RequestBody CreateMajorRequest request) {
		return majorConfigService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a major")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		majorConfigService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
