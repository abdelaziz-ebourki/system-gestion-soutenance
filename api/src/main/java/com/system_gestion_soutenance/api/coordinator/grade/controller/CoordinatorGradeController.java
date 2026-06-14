package com.system_gestion_soutenance.api.coordinator.grade.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.coordinator.grade.dto.GradeWeightedAverageResponse;
import com.system_gestion_soutenance.api.coordinator.grade.service.CoordinatorGradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator/grades")
@Tag(name = "Coordinator - Grades", description = "Grade Consultation")
public class CoordinatorGradeController {

	private final CoordinatorGradeService gradeService;

	public CoordinatorGradeController(CoordinatorGradeService gradeService) {
		this.gradeService = gradeService;
	}

	@GetMapping
	@Operation(summary = "Get all grades with weighted averages")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved grades"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")})
	public ApiResponse<List<GradeWeightedAverageResponse>> getGrades() {
		return ApiResponse.success(gradeService.getGrades());
	}
}
