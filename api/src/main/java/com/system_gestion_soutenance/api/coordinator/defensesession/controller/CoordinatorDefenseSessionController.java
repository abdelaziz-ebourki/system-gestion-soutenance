package com.system_gestion_soutenance.api.coordinator.defensesession.controller;

import com.system_gestion_soutenance.api.admin.defensesession.dto.DefenseSessionDto;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.coordinator.defensesession.dto.CreateDefenseSessionRequest;
import com.system_gestion_soutenance.api.coordinator.defensesession.dto.StatusTransitionRequest;
import com.system_gestion_soutenance.api.coordinator.defensesession.service.CoordinatorDefenseSessionService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.DefenseSessionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ApiResponse<List<DefenseSessionDto>> findAll() {
		return ApiResponse.success(service.findAll().stream().map(mapper::toDto).toList());
	}

	@PostMapping
	@Operation(summary = "Create a new defense session")
	public ResponseEntity<ApiResponse<DefenseSessionDto>> create(
			@Valid @RequestBody CreateDefenseSessionRequest request) {
		DefenseSession ds = service.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(mapper.toDto(ds)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a defense session")
	public ApiResponse<DefenseSessionDto> update(@PathVariable Long id,
			@Valid @RequestBody CreateDefenseSessionRequest request) {
		return ApiResponse.success(mapper.toDto(service.update(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a defense session")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Session supprimée avec succès.", null));
	}

	@PostMapping("/{id}/transition")
	@Operation(summary = "Transition a defense session to a new status")
	public ApiResponse<DefenseSessionDto> transition(@PathVariable Long id,
			@Valid @RequestBody StatusTransitionRequest request) {
		return ApiResponse.success(mapper.toDto(service.transition(id, request.toStatus())));
	}
}