package com.system_gestion_soutenance.api.coordinator.config.controller;

import com.system_gestion_soutenance.api.admin.config.juryrole.dto.JuryRoleTemplateDto;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.service.JuryRoleTemplateService;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import com.system_gestion_soutenance.api.coordinator.config.dto.DefenseSettingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/coordinator/config")
@PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
@Tag(name = "Coordinator - Config", description = "Endpoints for coordinators to view configuration")
public class CoordinatorConfigController {

	private final JuryRoleTemplateService juryRoleTemplateService;
	private final ConfigMapper configMapper;
	private final DefenseSessionRepository defenseSessionRepository;

	public CoordinatorConfigController(JuryRoleTemplateService juryRoleTemplateService, ConfigMapper configMapper,
			DefenseSessionRepository defenseSessionRepository) {
		this.juryRoleTemplateService = juryRoleTemplateService;
		this.configMapper = configMapper;
		this.defenseSessionRepository = defenseSessionRepository;
	}

	@GetMapping("/jury-role-templates")
	@Operation(summary = "List all jury role templates")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved jury role templates")})
	public ApiResponse<PaginatedResponse<JuryRoleTemplateDto>> findAllJuryRoleTemplates(
			@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
			@Parameter(description = "Items per page (1-500)") @RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		PaginatedResponse<JuryRoleTemplate> result = juryRoleTemplateService.findAll(page, limit);
		List<JuryRoleTemplateDto> items = result.items().stream().map(configMapper::toJuryRoleTemplateDto).toList();
		PaginatedResponse<JuryRoleTemplateDto> mapped = new PaginatedResponse<>(items, result.total(),
				result.pageCount(), result.currentPage(), result.size());
		return ApiResponse.success("Liste des templates de rôles récupérée avec succès", mapped);
	}

	@GetMapping("/settings")
	@Operation(summary = "Get defense settings", description = "Returns the defense settings from the currently active session.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved settings")})
	public ApiResponse<DefenseSettingsResponse> getSettings() {
		DefenseSession activeSession = defenseSessionRepository.findActiveSession(LocalDate.now()).orElse(null);
		if (activeSession == null) {
			return ApiResponse.success("Paramètres récupérés avec succès", null);
		}
		return ApiResponse.success("Paramètres récupérés avec succès", DefenseSettingsResponse.from(activeSession));
	}
}
