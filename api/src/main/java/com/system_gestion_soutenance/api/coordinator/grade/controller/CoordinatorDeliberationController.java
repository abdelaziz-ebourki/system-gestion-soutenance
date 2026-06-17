package com.system_gestion_soutenance.api.coordinator.grade.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.coordinator.grade.dto.DeliberationRequest;
import com.system_gestion_soutenance.api.coordinator.grade.dto.DeliberationStateResponse;
import com.system_gestion_soutenance.api.coordinator.grade.dto.ScoreAdjustRequest;
import com.system_gestion_soutenance.api.coordinator.grade.service.CoordinatorDeliberationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator")
@Tag(name = "Coordinator - Deliberation", description = "Endpoints for deliberation workflow")
public class CoordinatorDeliberationController {

	private final CoordinatorDeliberationService deliberationService;

	public CoordinatorDeliberationController(CoordinatorDeliberationService deliberationService) {
		this.deliberationService = deliberationService;
	}

	@GetMapping("/sessions/{id}/deliberation")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Get deliberation state", description = "Retrieves the deliberation state for a defense session.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved deliberation state"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")})
	public ApiResponse<DeliberationStateResponse> getDeliberationState(
			@Parameter(description = "Session ID") @PathVariable Long id) {
		return ApiResponse.success(deliberationService.getDeliberationState(id));
	}

	@PostMapping("/sessions/{id}/deliberate")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Finalize deliberation", description = "Finalizes the deliberation for a defense session.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deliberation finalized"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid deliberation data"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")})
	public ApiResponse<DeliberationStateResponse> deliberate(
			@Parameter(description = "Session ID") @PathVariable Long id,
			@Valid @RequestBody DeliberationRequest request) {
		return ApiResponse.success(deliberationService.deliberate(id, request));
	}

	@PatchMapping("/defenses/{id}/adjust-score")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Adjust defense score", description = "Adjusts the final score of a defense after deliberation.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Score adjusted"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid score"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Defense not found")})
	public ApiResponse<Void> adjustScore(@Parameter(description = "Defense ID") @PathVariable Long id,
			@Valid @RequestBody ScoreAdjustRequest request) {
		deliberationService.adjustScore(id, request);
		return ApiResponse.success("Note ajustée avec succès", null);
	}
}
