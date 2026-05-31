package com.system_gestion_soutenance.api.admin.config.general.controller;

import com.system_gestion_soutenance.api.admin.config.general.dto.UpdateGeneralSettingsRequest;
import com.system_gestion_soutenance.api.admin.config.general.entity.GeneralSettings;
import com.system_gestion_soutenance.api.admin.config.general.service.GeneralSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/config/general")
@Tag(name = "Admin - General Settings", description = "Paramètres généraux")
public class GeneralSettingsController {

	private final GeneralSettingsService service;

	public GeneralSettingsController(GeneralSettingsService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Get general settings")
	public GeneralSettings get() {
		return service.get();
	}

	@PutMapping
	@Operation(summary = "Update general settings")
	public GeneralSettings update(@Valid @RequestBody UpdateGeneralSettingsRequest updates) {
		return service.update(updates);
	}
}
