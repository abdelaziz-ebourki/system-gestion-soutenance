package com.system_gestion_soutenance.api.coordinator.schedule.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.ScheduleMapper;
import com.system_gestion_soutenance.api.coordinator.conflict.dto.ConflictDetailResponse;
import com.system_gestion_soutenance.api.coordinator.conflict.service.ConflictDetectionService;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.service.DefenseService;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.DefenseSessionIdRequest;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator/schedules")
@PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
@Tag(name = "Coordinator - Scheduling", description = "Endpoints for managing the defense schedule")
public class ScheduleController {

	private final DefenseService defenseService;
	private final ConflictDetectionService conflictDetectionService;
	private final ScheduleMapper scheduleMapper;

	public ScheduleController(DefenseService defenseService, ConflictDetectionService conflictDetectionService,
			ScheduleMapper scheduleMapper) {
		this.defenseService = defenseService;
		this.conflictDetectionService = conflictDetectionService;
		this.scheduleMapper = scheduleMapper;
	}

	@GetMapping
	@Operation(summary = "Get current schedule", description = "Retrieves the currently saved schedule for the active session.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved schedule")})
	public ApiResponse<List<ScheduleResponse>> get() {
		List<Defense> defenses = defenseService.getSchedule();
		Map<Long, Project> projectMap = defenseService.buildProjectMap(defenses);
		Map<Long, List<String>> studentNamesMap = defenseService.buildStudentNamesMap(projectMap);
		List<ScheduleResponse> response = defenses.stream()
				.map(d -> scheduleMapper.toDto(d, projectMap, studentNamesMap)).toList();
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
		List<Defense> defenses = defenseService.saveSchedule(request);
		Map<Long, Project> projectMap = defenseService.buildProjectMap(defenses);
		Map<Long, List<String>> studentNamesMap = defenseService.buildStudentNamesMap(projectMap);
		List<ScheduleResponse> response = defenses.stream()
				.map(d -> scheduleMapper.toDto(d, projectMap, studentNamesMap)).toList();
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/generation")
	@Operation(summary = "Auto-generate schedule", description = "Triggers the automatic generation of a proposed schedule based on constraints.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedule generated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid session ID")})
	public ApiResponse<List<ScheduleResponse>> autoGenerate(@Valid @RequestBody DefenseSessionIdRequest request) {
		List<ScheduleResponse> schedule = defenseService.autoGenerate(request.defenseSessionId());
		return ApiResponse.success(schedule);
	}

	@PatchMapping("/publication")
	@Operation(summary = "Publish schedule", description = "Marks the current schedule as published and visible to students/teachers.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedule published successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")})
	public ApiResponse<Void> publish(@Valid @RequestBody DefenseSessionIdRequest request) {
		defenseService.publish(request.defenseSessionId());
		return ApiResponse.success("Planning publié avec succès.", null);
	}
}