package com.system_gestion_soutenance.api.teacher.stats.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
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
	@Operation(summary = "Get statistics for the connected teacher")
	public ApiResponse<TeacherStatsResponse> getStats() {
		return ApiResponse.success(statsService.getStats(securityService.getCurrentUserId()));
	}
}
