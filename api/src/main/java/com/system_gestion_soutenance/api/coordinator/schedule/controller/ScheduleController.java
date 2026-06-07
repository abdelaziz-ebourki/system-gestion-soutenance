package com.system_gestion_soutenance.api.coordinator.schedule.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.ScheduleMapper;
import com.system_gestion_soutenance.api.coordinator.conflict.dto.ConflictDetailResponse;
import com.system_gestion_soutenance.api.coordinator.conflict.service.ConflictDetectionService;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.*;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coordinator/schedules")
@Tag(name = "Coordinator - Scheduling", description = "Endpoints for managing the defense schedule")
public class ScheduleController {

	private final ScheduleService scheduleService;
	private final ConflictDetectionService conflictDetectionService;
	private final ScheduleMapper scheduleMapper;

	public ScheduleController(ScheduleService scheduleService, ConflictDetectionService conflictDetectionService,
			ScheduleMapper scheduleMapper) {
		this.scheduleService = scheduleService;
		this.conflictDetectionService = conflictDetectionService;
		this.scheduleMapper = scheduleMapper;
	}

	@GetMapping
	@Operation(summary = "Get current schedule", description = "Retrieves the currently saved schedule for the active session.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved schedule")})
	public ApiResponse<List<ScheduleResponse>> get() {
		List<SlotAssignment> slots = scheduleService.getSchedule();
		Map<Long, Project> projectMap = scheduleService.buildProjectMap(slots);
		Map<Long, List<String>> studentNamesMap = scheduleService.buildStudentNamesMap(projectMap);
		List<ScheduleResponse> response = slots.stream().map(s -> scheduleMapper.toDto(s, projectMap, studentNamesMap))
				.toList();
		return ApiResponse.success(response);
	}

	@PostMapping
	@Operation(summary = "Save schedule", description = "Saves a new schedule after validating for conflicts.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedule saved successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Conflicts detected in the proposed schedule")})
	public ResponseEntity<ApiResponse<List<ScheduleResponse>>> save(@Valid @RequestBody ScheduleRequest request) {
		List<ConflictDetailResponse> conflicts = conflictDetectionService.validate(request,
				request.defenseSessionId().toString());
		if (!conflicts.isEmpty()) {
			boolean hasError = conflicts.stream().anyMatch(c -> "error".equals(c.severity()));
			if (hasError) {
				return ResponseEntity.badRequest().body(
						ApiResponse.error("Conflicts detected", conflicts.stream().map(c -> c.message()).toList()));
			}
		}
		List<SlotAssignment> slots = scheduleService.saveSchedule(request);
		Map<Long, Project> projectMap = scheduleService.buildProjectMap(slots);
		Map<Long, List<String>> studentNamesMap = scheduleService.buildStudentNamesMap(projectMap);
		List<ScheduleResponse> response = slots.stream().map(s -> scheduleMapper.toDto(s, projectMap, studentNamesMap))
				.toList();
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/generation")
	@Operation(summary = "Auto-generate schedule", description = "Triggers the automatic generation of a proposed schedule based on constraints.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedule generated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid session ID")})
	public ApiResponse<List<ScheduleResponse>> autoGenerate(@Valid @RequestBody DefenseSessionIdRequest request) {
		List<ScheduleResponse> schedule = scheduleService.autoGenerate(request.defenseSessionId());
		return ApiResponse.success(schedule);
	}

	@PatchMapping("/publication")
	@Operation(summary = "Publish schedule", description = "Marks the current schedule as published and visible to students/teachers.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedule published successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")})
	public ApiResponse<Void> publish(@Valid @RequestBody DefenseSessionIdRequest request) {
		scheduleService.publish(request.defenseSessionId());
		return ApiResponse.success("Planning publié avec succès.", null);
	}
}
