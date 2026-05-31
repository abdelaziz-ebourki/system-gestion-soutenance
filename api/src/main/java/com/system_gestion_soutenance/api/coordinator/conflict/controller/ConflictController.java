package com.system_gestion_soutenance.api.coordinator.conflict.controller;

import com.system_gestion_soutenance.api.coordinator.conflict.service.ConflictDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coordinator/schedule")
@Tag(name = "Coordinator - Conflict Detection", description = "Détection de conflits dans le planning")
public class ConflictController {

	private final ConflictDetectionService conflictDetectionService;

	public ConflictController(ConflictDetectionService conflictDetectionService) {
		this.conflictDetectionService = conflictDetectionService;
	}

	@PostMapping("/validate")
	@Operation(summary = "Validate a schedule for conflicts")
	public Map<String, List<Map<String, Object>>> validate(@RequestBody Map<String, Object> body) {
		@SuppressWarnings("unchecked")
		Map<String, Map<String, Object>> schedule = (Map<String, Map<String, Object>>) body.get("schedule");
		String defenseSessionId = (String) body.get("defenseSessionId");

		List<Map<String, Object>> conflicts = conflictDetectionService.validate(schedule, defenseSessionId);
		return Map.of("conflicts", conflicts);
	}
}
