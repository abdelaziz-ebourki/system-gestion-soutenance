package com.system_gestion_soutenance.api.coordinator.stats.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.coordinator.stats.dto.CoordinatorStatsResponse;
import com.system_gestion_soutenance.api.coordinator.stats.service.CoordinatorStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator/stats")
@PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
@Tag(name = "Coordinator - Stats", description = "Coordinator Statistics")
public class CoordinatorStatsController {

	private final CoordinatorStatsService statsService;

	public CoordinatorStatsController(CoordinatorStatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping
	@Operation(summary = "Get coordinator statistics")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")})
	public ApiResponse<CoordinatorStatsResponse> getStats() {
		return ApiResponse.success(statsService.getStats());
	}
}
