package com.system_gestion_soutenance.api.teacher.unavailability.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.teacher.unavailability.dto.TeacherUnavailabilityRequest;
import com.system_gestion_soutenance.api.teacher.unavailability.dto.TeacherUnavailabilityResponse;
import com.system_gestion_soutenance.api.teacher.unavailability.service.TeacherUnavailabilityService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/unavailabilities")
@Tag(name = "Teacher - Unavailability Management", description = "Endpoints for teachers to manage their unavailability slots")
public class TeacherUnavailabilityController {

	private final TeacherUnavailabilityService service;

	public TeacherUnavailabilityController(TeacherUnavailabilityService service) {
		this.service = service;
	}

	private static TeacherUnavailabilityResponse toResponse(List<Unavailability> entities) {
		Map<String, List<String>> slotsByDate = new LinkedHashMap<>();
		for (Unavailability u : entities) {
			slotsByDate.put(u.getDate(), u.getSlots());
		}
		return new TeacherUnavailabilityResponse(slotsByDate);
	}

	@GetMapping
	@Operation(summary = "Get unavailabilities", description = "Retrieves unavailability slots for the connected teacher.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved unavailabilities")})
	public ApiResponse<TeacherUnavailabilityResponse> get(@AuthenticationPrincipal User user) {
		return ApiResponse.success(toResponse(service.getByTeacher(user.getId())));
	}

	@PostMapping
	@Operation(summary = "Save unavailabilities", description = "Saves or updates unavailability slots for the connected teacher.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unavailabilities saved successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid unavailability data")})
	public ApiResponse<TeacherUnavailabilityResponse> save(@Valid @RequestBody TeacherUnavailabilityRequest request,
			@AuthenticationPrincipal User user) {
		return ApiResponse.success(toResponse(service.saveForTeacher(user.getId(), request)));
	}
}
