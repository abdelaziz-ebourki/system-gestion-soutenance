package com.system_gestion_soutenance.api.coordinator.defense.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.coordinator.defense.service.DefenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator/defenses")
@Tag(name = "Coordinator - Defenses", description = "Individual Defense Management")
public class CoordinatorDefenseController {

	private final DefenseService defenseService;

	public CoordinatorDefenseController(DefenseService defenseService) {
		this.defenseService = defenseService;
	}

	@PostMapping("/{id}/cancel")
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
}
