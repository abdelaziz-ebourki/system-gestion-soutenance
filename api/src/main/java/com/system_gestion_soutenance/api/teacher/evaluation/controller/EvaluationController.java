package com.system_gestion_soutenance.api.teacher.evaluation.controller;

import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationResponse;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationSubmitRequest;
import com.system_gestion_soutenance.api.teacher.evaluation.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/evaluations")
@Tag(name = "Teacher - Evaluations", description = "Gestion des évaluations")
public class EvaluationController {

	private final EvaluationService evaluationService;
	private final SecurityService securityService;

	public EvaluationController(EvaluationService evaluationService, SecurityService securityService) {
		this.evaluationService = evaluationService;
		this.securityService = securityService;
	}

	@GetMapping
	@Operation(summary = "List evaluations assigned to the connected teacher")
	public List<EvaluationResponse> findByTeacher() {
		return evaluationService.findByTeacher(securityService.getCurrentUserId());
	}

	@PostMapping("/{id}")
	@Operation(summary = "Submit an evaluation score and comment")
	public EvaluationResponse submit(@PathVariable Long id, @Valid @RequestBody EvaluationSubmitRequest request) {
		return evaluationService.submit(id, request);
	}
}
