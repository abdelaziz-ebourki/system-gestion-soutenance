package com.system_gestion_soutenance.api.teacher.unavailability.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.teacher.unavailability.dto.TeacherUnavailabilityRequest;
import com.system_gestion_soutenance.api.teacher.unavailability.dto.TeacherUnavailabilityResponse;
import com.system_gestion_soutenance.api.teacher.unavailability.service.TeacherUnavailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/unavailability")
@Tag(name = "Teacher - Unavailability", description = "Gestion des indisponibilités")
public class TeacherUnavailabilityController {

	private final TeacherUnavailabilityService service;
	private final SecurityService securityService;

	public TeacherUnavailabilityController(TeacherUnavailabilityService service, SecurityService securityService) {
		this.service = service;
		this.securityService = securityService;
	}

	private static TeacherUnavailabilityResponse toResponse(List<Unavailability> entities) {
		Map<String, List<String>> slotsByDate = new LinkedHashMap<>();
		for (Unavailability u : entities) {
			slotsByDate.put(u.getDate(), u.getSlots());
		}
		return new TeacherUnavailabilityResponse(slotsByDate);
	}

	@GetMapping
	@Operation(summary = "Get unavailability for the connected teacher")
	public ApiResponse<TeacherUnavailabilityResponse> get() {
		return ApiResponse.success(toResponse(service.getByTeacher(securityService.getCurrentUserId())));
	}

	@PostMapping
	@Operation(summary = "Save unavailability slots for the connected teacher")
	public ApiResponse<TeacherUnavailabilityResponse> save(@Valid @RequestBody TeacherUnavailabilityRequest request) {
		return ApiResponse.success(toResponse(service.saveForTeacher(securityService.getCurrentUserId(), request)));
	}
}
