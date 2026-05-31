package com.system_gestion_soutenance.api.coordinator.defensesession.controller;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.coordinator.defensesession.dto.CreateDefenseSessionRequest;
import com.system_gestion_soutenance.api.coordinator.defensesession.service.CoordinatorDefenseSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coordinator/defense-sessions")
@Tag(name = "Coordinator - Defense Sessions", description = "Gestion des sessions de soutenance")
public class CoordinatorDefenseSessionController {

	private final CoordinatorDefenseSessionService service;

	public CoordinatorDefenseSessionController(CoordinatorDefenseSessionService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "List all defense sessions")
	public List<DefenseSession> findAll() {
		return service.findAll();
	}

	@PostMapping
	@Operation(summary = "Create a new defense session")
	public ResponseEntity<DefenseSession> create(@Valid @RequestBody CreateDefenseSessionRequest request) {
		DefenseSession ds = service.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ds);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a defense session")
	public DefenseSession update(@PathVariable Long id, @Valid @RequestBody CreateDefenseSessionRequest request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a defense session")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/transition")
	@Operation(summary = "Transition a defense session to a new status")
	public DefenseSession transition(@PathVariable Long id, @RequestBody Map<String, String> body) {
		return service.transition(id, body.get("toStatus"));
	}
}
