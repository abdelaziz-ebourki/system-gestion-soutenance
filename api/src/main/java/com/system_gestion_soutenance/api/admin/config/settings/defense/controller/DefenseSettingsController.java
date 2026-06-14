package com.system_gestion_soutenance.api.admin.config.settings.defense.controller;

import com.system_gestion_soutenance.api.admin.config.settings.defense.dto.PatchDefenseSettingsRequest;
import com.system_gestion_soutenance.api.admin.config.settings.defense.dto.UpdateDefenseSettingsRequest;
import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.service.DefenseSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

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
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved defense settings")})
	public DefenseSettings get() {
		return service.get();
	}

	@PutMapping
	@Operation(summary = "Update defense settings")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Defense settings updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid settings data")})
	public DefenseSettings update(@Valid @RequestBody UpdateDefenseSettingsRequest updates) {
		return service.update(updates);
	}

	@PatchMapping
	@Operation(summary = "Partially update defense settings")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Defense settings partially updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid settings data")})
	public DefenseSettings patch(@Valid @RequestBody PatchDefenseSettingsRequest updates) {
		return service.patch(updates);
	}
}
