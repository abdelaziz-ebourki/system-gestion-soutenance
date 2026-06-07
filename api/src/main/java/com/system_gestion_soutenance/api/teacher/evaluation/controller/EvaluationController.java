package com.system_gestion_soutenance.api.teacher.evaluation.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.EvaluationMapper;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationResponse;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationSubmitRequest;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.service.EvaluationService;
import com.system_gestion_soutenance.api.user.entity.User;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/evaluations")
@Tag(name = "Teacher - Evaluation Management", description = "Endpoints for teachers to submit and view evaluations")
public class EvaluationController {

	private final EvaluationService evaluationService;
	private final EvaluationMapper evaluationMapper;

	public EvaluationController(EvaluationService evaluationService, EvaluationMapper evaluationMapper) {
		this.evaluationService = evaluationService;
		this.evaluationMapper = evaluationMapper;
	}

	@GetMapping
	@Operation(summary = "List evaluations", description = "Retrieves all evaluations assigned to the connected teacher.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved evaluations")})
	public ApiResponse<List<EvaluationResponse>> findByTeacher(@AuthenticationPrincipal User user) {
		Long teacherId = user.getId();
		List<Evaluation> evaluations = evaluationService.findByTeacher(teacherId);
		Map<Long, Project> projectMap = evaluationService.buildProjectMap(evaluations);
		return ApiResponse.success(evaluations.stream().map(e -> evaluationMapper.toDto(e, projectMap)).toList());
	}

	@PostMapping("/{id}")
	@Operation(summary = "Submit evaluation", description = "Submits the score and comments for a specific evaluation.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Evaluation submitted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid evaluation data"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Evaluation not found")})
	public ApiResponse<EvaluationResponse> submit(@PathVariable Long id,
			@Valid @RequestBody EvaluationSubmitRequest request) {
		Evaluation evaluation = evaluationService.submit(id, request);
		Map<Long, Project> projectMap = evaluationService.buildProjectMap(List.of(evaluation));
		return ApiResponse.success(evaluationMapper.toDto(evaluation, projectMap));
	}
}
