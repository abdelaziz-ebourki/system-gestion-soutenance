package com.system_gestion_soutenance.api.coordinator.grade.controller;

import com.system_gestion_soutenance.api.coordinator.grade.service.CoordinatorGradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coordinator/grades")
@Tag(name = "Coordinator - Grades", description = "Consultation des notes")
public class CoordinatorGradeController {

	private final CoordinatorGradeService gradeService;

	public CoordinatorGradeController(CoordinatorGradeService gradeService) {
		this.gradeService = gradeService;
	}

	@GetMapping
	@Operation(summary = "Get all grades with weighted averages")
	public List<Map<String, Object>> getGrades() {
		return gradeService.getGrades();
	}
}
