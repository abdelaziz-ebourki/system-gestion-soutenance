package com.system_gestion_soutenance.api.coordinator.conflict.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.coordinator.conflict.dto.ConflictDetailResponse;
import com.system_gestion_soutenance.api.coordinator.conflict.dto.ValidateScheduleRequest;
import com.system_gestion_soutenance.api.coordinator.conflict.service.ConflictDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coordinator/conflicts")
@Tag(name = "Coordinator - Conflicts", description = "Schedule Conflict Detection & Validation")
public class ConflictController {

	private final ConflictDetectionService conflictDetectionService;

	public ConflictController(ConflictDetectionService conflictDetectionService) {
		this.conflictDetectionService = conflictDetectionService;
	}

	@PostMapping("/validate")
	@Operation(summary = "Validate a schedule for conflicts")
	public ApiResponse<List<ConflictDetailResponse>> validate(@Valid @RequestBody ValidateScheduleRequest request) {
		List<ConflictDetailResponse> conflicts = conflictDetectionService
				.validate(
						new com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest(
								request.defenseSessionId(), request.schedule()),
						String.valueOf(request.defenseSessionId()));
		return ApiResponse.success(conflicts);
	}
}
