package com.system_gestion_soutenance.api.coordinator.defense.controller;

import com.system_gestion_soutenance.api.coordinator.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coordinator/defenses")
@Tag(name = "Coordinator - Defenses", description = "Gestion des soutenances individuelles")
public class CoordinatorDefenseController {

	private final ScheduleService scheduleService;

	public CoordinatorDefenseController(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	@PostMapping("/{id}/cancel")
	@Operation(summary = "Cancel a scheduled defense")
	public ResponseEntity<Map<String, String>> cancel(@PathVariable Long id) {
		scheduleService.cancelDefense(id);
		return ResponseEntity.ok(Map.of("message", "Soutenance annulée."));
	}
}
