package com.system_gestion_soutenance.api.student.stats.controller;

import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.student.stats.dto.StudentStatsResponse;
import com.system_gestion_soutenance.api.student.stats.service.StudentStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/stats")
@Tag(name = "Student - Stats", description = "Statistiques personnelles de l'étudiant")
public class StudentStatsController {

	private final StudentStatsService statsService;
	private final SecurityService securityService;

	public StudentStatsController(StudentStatsService statsService, SecurityService securityService) {
		this.statsService = statsService;
		this.securityService = securityService;
	}

	@GetMapping
	@Operation(summary = "Get personal statistics for the connected student")
	public StudentStatsResponse getStats() {
		return statsService.getStats(securityService.getCurrentUserId());
	}
}
