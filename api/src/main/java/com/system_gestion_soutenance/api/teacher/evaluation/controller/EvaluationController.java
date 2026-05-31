package com.system_gestion_soutenance.api.teacher.evaluation.controller;

import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationSubmitRequest;
import com.system_gestion_soutenance.api.teacher.evaluation.service.EvaluationService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/evaluations")
@Tag(name = "Teacher - Evaluations", description = "Gestion des évaluations")
public class EvaluationController {

	private final EvaluationService evaluationService;

	public EvaluationController(EvaluationService evaluationService) {
		this.evaluationService = evaluationService;
	}

	@GetMapping
	@Operation(summary = "List evaluations assigned to the connected teacher")
	public List<Map<String, Object>> findByTeacher() {
		return evaluationService.findByTeacher(getCurrentUserId());
	}

	@PostMapping("/{id}")
	@Operation(summary = "Submit an evaluation score and comment")
	public Map<String, Object> submit(@PathVariable Long id, @Valid @RequestBody EvaluationSubmitRequest request) {
		return evaluationService.submit(id, request);
	}

	private Long getCurrentUserId() {
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
	}
}
