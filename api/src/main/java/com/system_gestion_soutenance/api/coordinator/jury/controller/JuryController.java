package com.system_gestion_soutenance.api.coordinator.jury.controller;

import com.system_gestion_soutenance.api.coordinator.jury.dto.CreateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.service.JuryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
	public List<Map<String, Object>> findAll() {
		return juryService.findAll();
	}

	@PostMapping
	@Operation(summary = "Create a new jury")
	public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateJuryRequest request) {
		Map<String, Object> jury = juryService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(jury);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a jury")
	public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
		return juryService.update(id, updates);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a jury")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		juryService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
