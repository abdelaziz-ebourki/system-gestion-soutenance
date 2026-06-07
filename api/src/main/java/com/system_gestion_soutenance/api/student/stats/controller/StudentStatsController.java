package com.system_gestion_soutenance.api.student.stats.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.student.stats.dto.StudentStatsResponse;
import com.system_gestion_soutenance.api.student.stats.service.StudentStatsService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/stats")
@Tag(name = "Student - Statistics", description = "Endpoints for students to view their personal statistics")
public class StudentStatsController {

	private final StudentStatsService statsService;

	public StudentStatsController(StudentStatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping
	@Operation(summary = "Get personal statistics", description = "Retrieves personal statistics for the connected student.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")})
	public ApiResponse<StudentStatsResponse> getStats(@AuthenticationPrincipal User user) {
		return ApiResponse.success(statsService.getStats(user.getId()));
	}
}
