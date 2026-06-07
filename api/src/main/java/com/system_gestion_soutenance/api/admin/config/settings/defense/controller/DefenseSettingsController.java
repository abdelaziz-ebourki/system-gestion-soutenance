package com.system_gestion_soutenance.api.admin.config.settings.defense.controller;

import com.system_gestion_soutenance.api.admin.config.settings.defense.dto.UpdateDefenseSettingsRequest;
import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.service.DefenseSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/config/settings")
@Tag(name = "Admin - Defense Settings", description = "Defense Settings")
public class DefenseSettingsController {

	private final DefenseSettingsService service;

	public DefenseSettingsController(DefenseSettingsService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Get defense settings")
	public DefenseSettings get() {
		return service.get();
	}

	@PutMapping
	@Operation(summary = "Update defense settings")
	public DefenseSettings update(@Valid @RequestBody UpdateDefenseSettingsRequest updates) {
		return service.update(updates);
	}
}
