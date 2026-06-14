package com.system_gestion_soutenance.api.coordinator.defensesession.controller;

import com.system_gestion_soutenance.api.admin.defensesession.dto.DefenseSessionDto;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.coordinator.defensesession.dto.CreateDefenseSessionRequest;
import com.system_gestion_soutenance.api.coordinator.defensesession.dto.StatusTransitionRequest;
import com.system_gestion_soutenance.api.coordinator.defensesession.service.CoordinatorDefenseSessionService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.DefenseSessionMapper;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator/defense-sessions")
@Tag(name = "Coordinator - Defense Sessions", description = "Defense Session Management")
public class CoordinatorDefenseSessionController {

	private final CoordinatorDefenseSessionService service;
	private final DefenseSessionMapper mapper;

	public CoordinatorDefenseSessionController(CoordinatorDefenseSessionService service, DefenseSessionMapper mapper) {
		this.service = service;
		this.mapper = mapper;
	}

	@GetMapping
	@Operation(summary = "List all defense sessions")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved defense sessions"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid parameters")})
	public ApiResponse<PaginatedResponse<DefenseSessionDto>> findAll(
			@Parameter(description = "Page number") @RequestParam(defaultValue = "0") @Min(0) int page,
			@Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		PaginatedResponse<DefenseSession> result = service.findAll(page, limit);
		List<DefenseSessionDto> items = result.items().stream().map(mapper::toDto).toList();
		PaginatedResponse<DefenseSessionDto> mapped = new PaginatedResponse<>(items, result.total(), result.pageCount(),
				result.currentPage(), result.size());
		return ApiResponse.success(mapped);
	}

	@PostMapping
	@Operation(summary = "Create a new defense session")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Defense session created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data")})
	public ResponseEntity<ApiResponse<DefenseSessionDto>> create(
			@Valid @RequestBody CreateDefenseSessionRequest request) {
		DefenseSession ds = service.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(mapper.toDto(ds)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Update a defense session")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Defense session updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Defense session not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data")})
	public ApiResponse<DefenseSessionDto> update(@Parameter(description = "Defense session ID") @PathVariable Long id,
			@Valid @RequestBody CreateDefenseSessionRequest request) {
		return ApiResponse.success(mapper.toDto(service.update(id, request)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Delete a defense session")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Defense session deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Defense session not found")})
	public ResponseEntity<ApiResponse<Void>> delete(
			@Parameter(description = "Defense session ID") @PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Session supprimée avec succès.", null));
	}

	@PostMapping("/{id}/transition")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Transition a defense session to a new status")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status transition completed successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Defense session not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status transition")})
	public ApiResponse<DefenseSessionDto> transition(
			@Parameter(description = "Defense session ID") @PathVariable Long id,
			@Valid @RequestBody StatusTransitionRequest request) {
		return ApiResponse.success(mapper.toDto(service.transition(id, request.toStatus())));
	}

	@PatchMapping("/{id}/freeze")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Freeze a defense session", description = "Blocks all grade submissions for this session.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Defense session frozen successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Defense session not found")})
	public ApiResponse<DefenseSessionDto> freeze(@Parameter(description = "Defense session ID") @PathVariable Long id) {
		return ApiResponse.success(mapper.toDto(service.freeze(id)));
	}

	@PatchMapping("/{id}/unfreeze")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Unfreeze a defense session", description = "Re-enables grade submissions for this session.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Defense session unfrozen successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Defense session not found")})
	public ApiResponse<DefenseSessionDto> unfreeze(
			@Parameter(description = "Defense session ID") @PathVariable Long id) {
		return ApiResponse.success(mapper.toDto(service.unfreeze(id)));
	}

	@PatchMapping("/{id}/approve")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Approve a defense session", description = "Admin approves a defense session, enabling schedule publishing.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Defense session approved successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Defense session not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Unauthorized")})
	public ApiResponse<DefenseSessionDto> approve(@Parameter(description = "Defense session ID") @PathVariable Long id,
			@AuthenticationPrincipal User user) {
		return ApiResponse.success(mapper.toDto(service.approve(id, user.getId())));
	}

	@PatchMapping("/{id}/revoke-approval")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Revoke approval of a defense session", description = "Admin revokes approval, preventing schedule publishing.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval revoked successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Defense session not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Unauthorized")})
	public ApiResponse<DefenseSessionDto> revokeApproval(
			@Parameter(description = "Defense session ID") @PathVariable Long id) {
		return ApiResponse.success(mapper.toDto(service.revokeApproval(id)));
	}
}
