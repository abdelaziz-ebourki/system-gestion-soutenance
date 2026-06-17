package com.system_gestion_soutenance.api.coordinator.defense.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.coordinator.defense.entity.DefenseStatus;
import com.system_gestion_soutenance.api.coordinator.defense.service.DefenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator")
@Tag(name = "Coordinator - Defenses", description = "Individual Defense Management")
public class CoordinatorDefenseController {

	private final DefenseService defenseService;

	public CoordinatorDefenseController(DefenseService defenseService) {
		this.defenseService = defenseService;
	}

	@PostMapping("/defenses/{id}/cancel")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Cancel a scheduled defense")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Defense cancelled successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Defense not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid cancellation request")})
	public ApiResponse<Void> cancel(@Parameter(description = "Defense ID") @PathVariable Long id) {
		defenseService.cancelDefense(id);
		return ApiResponse.success("Soutenance annulée.", null);
	}

	@PatchMapping("/defenses/{id}/status")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Update defense status", description = "Transitions a defense to a new status.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Defense not found")})
	public ApiResponse<Void> updateStatus(@Parameter(description = "Defense ID") @PathVariable Long id,
			@Parameter(description = "New status") @RequestParam DefenseStatus status) {
		defenseService.updateStatus(id, status);
		return ApiResponse.success("Statut mis à jour.", null);
	}

	@PatchMapping("/sessions/{id}/start")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Start a session", description = "Sets all scheduled defenses in a session to IN_PROGRESS.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Session started"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")})
	public ApiResponse<Void> startSession(@Parameter(description = "Session ID") @PathVariable Long id) {
		defenseService.startSession(id);
		return ApiResponse.success("Session démarrée.", null);
	}
}
