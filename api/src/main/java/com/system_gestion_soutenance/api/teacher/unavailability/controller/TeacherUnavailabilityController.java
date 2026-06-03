package com.system_gestion_soutenance.api.teacher.unavailability.controller;

import com.system_gestion_soutenance.api.teacher.unavailability.service.TeacherUnavailabilityService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
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
	public Map<String, Object> get() {
		return service.getByTeacher(getCurrentUserId());
	}

	@SuppressWarnings("unchecked")
	@PostMapping
	@Operation(summary = "Save unavailability slots for the connected teacher")
	public Map<String, Object> save(@RequestBody Map<String, Object> body) {
		Map<String, List<String>> slotsByDate;
		if (body.get("slotsByDate") != null) {
			slotsByDate = (Map<String, List<String>>) body.get("slotsByDate");
		} else {
			slotsByDate = (Map<String, List<String>>) (Map) body;
		}
		return service.saveForTeacher(getCurrentUserId(), slotsByDate);
	}

	private Long getCurrentUserId() {
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
	}
}
