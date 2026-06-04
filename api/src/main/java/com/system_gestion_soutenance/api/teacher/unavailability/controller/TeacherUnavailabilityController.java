package com.system_gestion_soutenance.api.teacher.unavailability.controller;

import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.teacher.unavailability.dto.TeacherUnavailabilityRequest;
import com.system_gestion_soutenance.api.teacher.unavailability.dto.TeacherUnavailabilityResponse;
import com.system_gestion_soutenance.api.teacher.unavailability.service.TeacherUnavailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

	@GetMapping
	@Operation(summary = "Get unavailability for the connected teacher")
	public TeacherUnavailabilityResponse get() {
		return service.getByTeacher(securityService.getCurrentUserId());
	}

	@PostMapping
	@Operation(summary = "Save unavailability slots for the connected teacher")
	public TeacherUnavailabilityResponse save(@Valid @RequestBody TeacherUnavailabilityRequest request) {
		return service.saveForTeacher(securityService.getCurrentUserId(), request);
	}
}
