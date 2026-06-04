package com.system_gestion_soutenance.api.coordinator.schedule.controller;

import com.system_gestion_soutenance.api.coordinator.conflict.service.ConflictDetectionService;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleResponse;
import com.system_gestion_soutenance.api.coordinator.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
	public List<ScheduleResponse> get() {
		return scheduleService.getSchedule();
	}

	@PostMapping
	@Operation(summary = "Save schedule with conflict validation")
	public ResponseEntity<?> save(@Valid @RequestBody ScheduleRequest request) {
		List<ScheduleResponse> result = scheduleService.saveSchedule(request);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/auto-generate")
	@Operation(summary = "Auto-generate a proposed schedule")
	public ResponseEntity<List<ScheduleResponse>> autoGenerate(@RequestBody Map<String, String> body) {
		String defenseSessionId = body.get("defenseSessionId");
		if (defenseSessionId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le champ 'defenseSessionId' est requis");
		}
		List<ScheduleResponse> schedule = scheduleService.autoGenerate(Long.valueOf(defenseSessionId));
		return ResponseEntity.ok(schedule);
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
