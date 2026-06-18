package com.system_gestion_soutenance.api.admin.config.juryrole.controller;

import com.system_gestion_soutenance.api.admin.config.juryrole.dto.CreateJuryRoleTemplateRequest;
import com.system_gestion_soutenance.api.admin.config.juryrole.dto.JuryRoleTemplateDto;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.service.JuryRoleTemplateService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/admin/config/jury-role-templates")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
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
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved jury role templates")})
	public ApiResponse<PaginatedResponse<JuryRoleTemplateDto>> findAll(
			@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
			@Parameter(description = "Items per page (1-500)") @RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		PaginatedResponse<JuryRoleTemplate> result = juryRoleTemplateService.findAll(page, limit);
		List<JuryRoleTemplateDto> items = result.items().stream().map(configMapper::toJuryRoleTemplateDto).toList();
		PaginatedResponse<JuryRoleTemplateDto> mapped = new PaginatedResponse<>(items, result.total(),
				result.pageCount(), result.currentPage(), result.size());
		return ApiResponse.success("Liste des templates de rôles récupérée avec succès", mapped);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create a new jury role template")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Jury role template created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid jury role template data")})
	public ResponseEntity<ApiResponse<JuryRoleTemplateDto>> create(
			@Valid @RequestBody CreateJuryRoleTemplateRequest request) {
		JuryRoleTemplate template = juryRoleTemplateService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(
				ApiResponse.success("Template de rôle créé avec succès", configMapper.toJuryRoleTemplateDto(template)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Update a jury role template")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jury role template updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Jury role template not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<JuryRoleTemplateDto> update(
			@Parameter(description = "Jury role template ID") @PathVariable Long id,
			@Valid @RequestBody CreateJuryRoleTemplateRequest request) {
		return ApiResponse.success("Template de rôle mis à jour avec succès",
				configMapper.toJuryRoleTemplateDto(juryRoleTemplateService.update(id, request)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete a jury role template")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jury role template deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Jury role template not found")})
	public ResponseEntity<ApiResponse<Void>> delete(
			@Parameter(description = "Jury role template ID") @PathVariable Long id) {
		juryRoleTemplateService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Template de rôle supprimé avec succès", null));
	}
}
