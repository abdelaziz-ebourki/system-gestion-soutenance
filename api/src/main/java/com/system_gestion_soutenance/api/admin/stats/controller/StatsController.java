package com.system_gestion_soutenance.api.admin.stats.controller;

import com.system_gestion_soutenance.api.admin.stats.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@Tag(name = "Admin - Stats", description = "Statistiques globales")
public class StatsController {

	private final StatsService statsService;

	public StatsController(StatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping
	@Operation(summary = "Get global statistics")
	public Map<String, Object> getStats() {
		return statsService.getStats();
	}
}
