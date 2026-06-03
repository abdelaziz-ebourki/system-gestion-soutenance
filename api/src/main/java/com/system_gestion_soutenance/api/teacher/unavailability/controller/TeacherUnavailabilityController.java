package com.system_gestion_soutenance.api.teacher.unavailability.controller;

import com.system_gestion_soutenance.api.teacher.unavailability.dto.CreateUnavailabilityRequest;
import com.system_gestion_soutenance.api.teacher.unavailability.dto.TeacherUnavailabilityRequest;
import com.system_gestion_soutenance.api.teacher.unavailability.dto.TeacherUnavailabilityResponse;
import com.system_gestion_soutenance.api.teacher.unavailability.service.TeacherUnavailabilityService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/unavailability")
@Tag(name = "Teacher - Unavailability", description = "Gestion des indisponibilités")
public class TeacherUnavailabilityController {

	private final TeacherUnavailabilityService service;

	public TeacherUnavailabilityController(TeacherUnavailabilityService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Get unavailability for the connected teacher")
	public TeacherUnavailabilityResponse get() {
		return service.getByTeacher(getCurrentUserId());
	}

	@PostMapping
	@Operation(summary = "Save unavailability slots for the connected teacher")
	public TeacherUnavailabilityResponse save(@Valid @RequestBody TeacherUnavailabilityRequest request) {
		return service.saveForTeacher(getCurrentUserId(), request);
	}

	private Long getCurrentUserId() {
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
	}
}
