package com.system_gestion_soutenance.api.admin.config.juryrole.controller;

import com.system_gestion_soutenance.api.admin.config.juryrole.dto.CreateJuryRoleTemplateRequest;
import com.system_gestion_soutenance.api.admin.config.juryrole.dto.JuryRoleTemplateDto;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.service.JuryRoleTemplateService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/admin/config/jury-role-templates")
@Tag(name = "Admin - Jury Role Templates", description = "Jury Role Template Management")
public class JuryRoleTemplateController {

	private final JuryRoleTemplateService juryRoleTemplateService;
	private final ConfigMapper configMapper;

	public JuryRoleTemplateController(JuryRoleTemplateService juryRoleTemplateService, ConfigMapper configMapper) {
		this.juryRoleTemplateService = juryRoleTemplateService;
		this.configMapper = configMapper;
	}

	@GetMapping
	@Operation(summary = "List all jury role templates")
	public ApiResponse<List<JuryRoleTemplateDto>> findAll() {
		List<JuryRoleTemplateDto> templates = juryRoleTemplateService.findAll().stream()
				.map(configMapper::toJuryRoleTemplateDto).toList();
		return ApiResponse.success("Liste des templates de rôles récupérée avec succès", templates);
	}

	@PostMapping
	@Operation(summary = "Create a new jury role template")
	public ResponseEntity<ApiResponse<JuryRoleTemplateDto>> create(
			@Valid @RequestBody CreateJuryRoleTemplateRequest request) {
		JuryRoleTemplate template = juryRoleTemplateService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(
				ApiResponse.success("Template de rôle créé avec succès", configMapper.toJuryRoleTemplateDto(template)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a jury role template")
	public ApiResponse<JuryRoleTemplateDto> update(@PathVariable Long id,
			@Valid @RequestBody CreateJuryRoleTemplateRequest request) {
		return ApiResponse.success("Template de rôle mis à jour avec succès",
				configMapper.toJuryRoleTemplateDto(juryRoleTemplateService.update(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a jury role template")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		juryRoleTemplateService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Template de rôle supprimé avec succès", null));
	}
}