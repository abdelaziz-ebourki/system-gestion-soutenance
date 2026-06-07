package com.system_gestion_soutenance.api.coordinator.stats.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.coordinator.stats.dto.CoordinatorStatsResponse;
import com.system_gestion_soutenance.api.coordinator.stats.service.CoordinatorStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coordinator/stats")
@Tag(name = "Coordinator - Stats", description = "Coordinator Statistics")
public class CoordinatorStatsController {

	private final CoordinatorStatsService statsService;

	public CoordinatorStatsController(CoordinatorStatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping
	@Operation(summary = "Get coordinator statistics")
	public ApiResponse<CoordinatorStatsResponse> getStats() {
		return ApiResponse.success(statsService.getStats());
	}
}
