package com.system_gestion_soutenance.api.coordinator.schedule.controller;

import com.system_gestion_soutenance.api.coordinator.conflict.service.ConflictDetectionService;
import com.system_gestion_soutenance.api.coordinator.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/coordinator/schedule")
@Tag(name = "Coordinator - Schedule", description = "Gestion du planning des soutenances")
public class ScheduleController {

	private final ScheduleService scheduleService;
	private final ConflictDetectionService conflictDetectionService;

	public ScheduleController(ScheduleService scheduleService, ConflictDetectionService conflictDetectionService) {
		this.scheduleService = scheduleService;
		this.conflictDetectionService = conflictDetectionService;
	}

	@GetMapping
	@Operation(summary = "Get the current schedule")
	public Map<String, Map<String, Object>> get() {
		return scheduleService.getSchedule();
	}

	@SuppressWarnings("unchecked")
	@PostMapping
	@Operation(summary = "Save schedule with conflict validation")
	public ResponseEntity<?> save(@RequestBody Map<String, Object> body) {
		Object raw = body.get("schedule");
		if (raw == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le champ 'schedule' est requis");
		}
		String defenseSessionId = (String) body.get("defenseSessionId");
		Map<String, Map<String, Object>> schedule = (Map<String, Map<String, Object>>) raw;

		List<Map<String, Object>> conflicts = conflictDetectionService.validate(schedule, defenseSessionId);
		boolean hasErrors = conflicts.stream().anyMatch(c -> "error".equals(c.get("severity")));

		if (hasErrors) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("conflicts", conflicts));
		}

		Map<String, Map<String, Object>> result = scheduleService.saveSchedule(schedule);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/auto-generate")
	@Operation(summary = "Auto-generate a proposed schedule")
	public ResponseEntity<Map<String, Object>> autoGenerate(@RequestBody Map<String, String> body) {
		String defenseSessionId = body.get("defenseSessionId");
		if (defenseSessionId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le champ 'defenseSessionId' est requis");
		}
		Map<String, Map<String, Object>> schedule = scheduleService.autoGenerate(Long.valueOf(defenseSessionId));
		return ResponseEntity.ok(Map.of("schedule", schedule));
	}

	@PostMapping("/publish")
	@Operation(summary = "Publish the schedule")
	public ResponseEntity<Map<String, String>> publish(@RequestBody Map<String, String> body) {
		String defenseSessionId = body.get("defenseSessionId");
		if (defenseSessionId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le champ 'defenseSessionId' est requis");
		}
		scheduleService.publish(Long.valueOf(defenseSessionId));
		return ResponseEntity.ok(Map.of("message", "Planning publié avec succès."));
	}
}
