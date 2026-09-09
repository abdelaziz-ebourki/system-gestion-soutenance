package com.system_gestion_soutenance.api.teacher.stats.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.teacher.stats.dto.TeacherStatsResponse;
import com.system_gestion_soutenance.api.teacher.stats.service.TeacherStatsService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/teacher/stats")
@PreAuthorize("hasRole('TEACHER')")
@Tag(name = "Teacher - Statistics", description = "Endpoints for teachers to view their personal statistics")
public class TeacherStatsController {

	private final TeacherStatsService statsService;

	public TeacherStatsController(TeacherStatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping
	@Operation(summary = "Get personal statistics", description = "Retrieves personal statistics for the connected teacher.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")})
	public ApiResponse<TeacherStatsResponse> getStats(@AuthenticationPrincipal User user) {
		return ApiResponse.success(statsService.getStats(user.getId()));
	}
}