package com.system_gestion_soutenance.api.coordinator.schedule.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.coordinator.conflict.service.ConflictDetectionService;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.*;
import com.system_gestion_soutenance.api.coordinator.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
	public ApiResponse<List<ScheduleResponse>> get() {
		return ApiResponse.success(scheduleService.getSchedule());
	}

	@PostMapping
	@Operation(summary = "Save schedule with conflict validation")
	public ResponseEntity<ApiResponse<List<ScheduleResponse>>> save(@Valid @RequestBody ScheduleRequest request) {
		List<Map<String, Object>> conflicts = conflictDetectionService.validate(request, "Soutenance");
		if (!conflicts.isEmpty()) {
			boolean hasError = conflicts.stream().anyMatch(c -> "error".equals(c.get("severity")));
			if (hasError) {
				return ResponseEntity.badRequest().body(ApiResponse.error("Conflicts detected",
						conflicts.stream().map(c -> (String) c.get("message")).toList()));
			}
		}
		List<ScheduleResponse> result = scheduleService.saveSchedule(request);
		return ResponseEntity.ok(ApiResponse.success(result));
	}

	@PostMapping("/auto-generate")
	@Operation(summary = "Auto-generate a proposed schedule")
	public ApiResponse<List<ScheduleResponse>> autoGenerate(@Valid @RequestBody DefenseSessionIdRequest request) {
		List<ScheduleResponse> schedule = scheduleService.autoGenerate(request.defenseSessionId());
		return ApiResponse.success(schedule);
	}

	@PostMapping("/publish")
	@Operation(summary = "Publish the schedule")
	public ApiResponse<Void> publish(@Valid @RequestBody DefenseSessionIdRequest request) {
		scheduleService.publish(request.defenseSessionId());
		return ApiResponse.success("Planning publié avec succès.", null);
	}
}
