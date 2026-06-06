package com.system_gestion_soutenance.api.teacher.evaluation.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.EvaluationMapper;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationResponse;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationSubmitRequest;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.service.EvaluationService;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/evaluations")
@Tag(name = "Teacher - Evaluations", description = "Gestion des évaluations")
public class EvaluationController {

	private final EvaluationService evaluationService;
	private final SecurityService securityService;
	private final EvaluationMapper evaluationMapper;

	public EvaluationController(EvaluationService evaluationService, SecurityService securityService,
			EvaluationMapper evaluationMapper) {
		this.evaluationService = evaluationService;
		this.securityService = securityService;
		this.evaluationMapper = evaluationMapper;
	}

	@GetMapping
	@Operation(summary = "List evaluations assigned to the connected teacher")
	public ApiResponse<List<EvaluationResponse>> findByTeacher() {
		Long teacherId = securityService.getCurrentUserId();
		List<Evaluation> evaluations = evaluationService.findByTeacher(teacherId);
		Map<Long, Project> projectMap = evaluationService.buildProjectMap(evaluations);
		return ApiResponse.success(evaluations.stream().map(e -> evaluationMapper.toDto(e, projectMap)).toList());
	}

	@PostMapping("/{id}")
	@Operation(summary = "Submit an evaluation score and comment")
	public ApiResponse<EvaluationResponse> submit(@PathVariable Long id,
			@Valid @RequestBody EvaluationSubmitRequest request) {
		Evaluation evaluation = evaluationService.submit(id, request);
		Map<Long, Project> projectMap = evaluationService.buildProjectMap(List.of(evaluation));
		return ApiResponse.success(evaluationMapper.toDto(evaluation, projectMap));
	}
}
