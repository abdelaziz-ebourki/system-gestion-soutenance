package com.system_gestion_soutenance.api.admin.stats.controller;

import com.system_gestion_soutenance.api.admin.stats.dto.GlobalStatsResponse;
import com.system_gestion_soutenance.api.admin.stats.service.StatsService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Stats", description = "Global Statistics")
public class StatsController {

	private final StatsService statsService;

	public StatsController(StatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping
	@Operation(summary = "Get global statistics")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved global statistics")})
	public ApiResponse<GlobalStatsResponse> getStats() {
		return ApiResponse.success(statsService.getStats());
	}
}
