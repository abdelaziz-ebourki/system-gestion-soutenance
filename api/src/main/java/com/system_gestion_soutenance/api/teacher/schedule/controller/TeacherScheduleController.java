package com.system_gestion_soutenance.api.teacher.schedule.controller;

import com.system_gestion_soutenance.api.teacher.schedule.service.TeacherScheduleService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/schedule")
@Tag(name = "Teacher - Schedule", description = "Consultation du planning")
public class TeacherScheduleController {

	private final TeacherScheduleService service;

	public TeacherScheduleController(TeacherScheduleService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Get the schedule for the connected teacher")
	public List<Map<String, Object>> getSchedule() {
		return service.getSchedule(getCurrentUserId());
	}

	private Long getCurrentUserId() {
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
	}
}
