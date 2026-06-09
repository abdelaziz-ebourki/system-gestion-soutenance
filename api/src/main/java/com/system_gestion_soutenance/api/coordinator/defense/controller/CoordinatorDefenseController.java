package com.system_gestion_soutenance.api.coordinator.defense.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.coordinator.defense.service.DefenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
	@Operation(summary = "Cancel a scheduled defense")
	public ApiResponse<Void> cancel(@PathVariable Long id) {
		defenseService.cancelDefense(id);
		return ApiResponse.success("Soutenance annulée.", null);
	}
}