package com.system_gestion_soutenance.api.coordinator.jury.controller;

import com.system_gestion_soutenance.api.coordinator.jury.dto.CreateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.dto.JuryResponse;
import com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.service.DefenseService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.JuryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator/juries")
@Tag(name = "Coordinator - Jury Management", description = "Endpoints for managing defense juries")
public class JuryController {

	private final DefenseService defenseService;
	private final JuryMapper juryMapper;

	public JuryController(DefenseService defenseService, JuryMapper juryMapper) {
		this.defenseService = defenseService;
		this.juryMapper = juryMapper;
	}

	@GetMapping
	@Operation(summary = "List juries", description = "Retrieves all juries configured for the current session.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved juries")})
	public ApiResponse<PaginatedResponse<JuryResponse>> findAll(@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		PaginatedResponse<Defense> result = defenseService.getSchedule(page, limit);
		List<JuryResponse> items = result.items().stream().map(juryMapper::toDto).toList();
		PaginatedResponse<JuryResponse> mapped = new PaginatedResponse<>(items, result.total(), result.pageCount(),
				result.currentPage(), result.size());
		return ApiResponse.success("Liste des jurys récupérée avec succès", mapped);
	}

	@PostMapping
	@Operation(summary = "Create jury", description = "Creates a new jury.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Jury created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid jury data")})
	public ResponseEntity<ApiResponse<JuryResponse>> create(@Valid @RequestBody CreateJuryRequest request) {
		Defense defense = defenseService.createJury(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Jury créé avec succès", juryMapper.toDto(defense)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Update jury", description = "Updates an existing jury's details.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jury updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Jury not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<JuryResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateJuryRequest updates) {
		return ApiResponse.success("Jury mis à jour avec succès",
				juryMapper.toDto(defenseService.updateJury(id, updates)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Delete jury", description = "Removes a jury from the system.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jury deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Jury not found")})
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		defenseService.clearJuryMembers(id);
		return ResponseEntity.ok(ApiResponse.success("Jury supprimé avec succès", null));
	}
}