package com.system_gestion_soutenance.api.teacher.stats.controller;

import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.teacher.stats.dto.TeacherStatsResponse;
import com.system_gestion_soutenance.api.teacher.stats.service.TeacherStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/stats")
@Tag(name = "Teacher - Stats", description = "Statistiques personnelles de l'enseignant")
public class TeacherStatsController {

	private final TeacherStatsService statsService;
	private final SecurityService securityService;

	public TeacherStatsController(TeacherStatsService statsService, SecurityService securityService) {
		this.statsService = statsService;
		this.securityService = securityService;
	}

	@GetMapping
	@Operation(summary = "Get personal statistics for the connected teacher")
	public TeacherStatsResponse getStats() {
		return statsService.getStats(securityService.getCurrentUserId());
	}
}
