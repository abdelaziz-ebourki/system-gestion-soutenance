package com.system_gestion_soutenance.api.coordinator.jury.controller;

import com.system_gestion_soutenance.api.coordinator.jury.dto.CreateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.dto.JuryResponse;
import com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.service.JuryService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coordinator/juries")
@Tag(name = "Coordinator - Juries", description = "Gestion des jurys")
public class JuryController {

	private final JuryService juryService;

	public JuryController(JuryService juryService) {
		this.juryService = juryService;
	}

	@GetMapping
	@Operation(summary = "List all juries")
	public ApiResponse<List<JuryResponse>> findAll() {
		return ApiResponse.success("Liste des jurys récupérée avec succès", juryService.findAll());
	}

	@PostMapping
	@Operation(summary = "Create a new jury")
	public ResponseEntity<ApiResponse<JuryResponse>> create(@Valid @RequestBody CreateJuryRequest request) {
		JuryResponse jury = juryService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Jury créé avec succès", jury));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a jury")
	public ApiResponse<JuryResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateJuryRequest updates) {
		return ApiResponse.success("Jury mis à jour avec succès", juryService.update(id, updates));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a jury")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		juryService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Jury supprimé avec succès", null));
	}
}
