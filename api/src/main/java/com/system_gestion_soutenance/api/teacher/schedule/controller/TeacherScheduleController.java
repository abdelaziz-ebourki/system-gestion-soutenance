package com.system_gestion_soutenance.api.teacher.schedule.controller;

import com.system_gestion_soutenance.api.teacher.schedule.dto.TeacherScheduleResponse;
import com.system_gestion_soutenance.api.teacher.schedule.service.TeacherScheduleService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/schedule")
@Tag(name = "Teacher - Schedule", description = "Planning des soutenances pour l'enseignant")
public class TeacherScheduleController {

	private final TeacherScheduleService scheduleService;

	public TeacherScheduleController(TeacherScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	@GetMapping
	@Operation(summary = "Get the connected teacher's defense schedule")
	public TeacherScheduleResponse get() {
		return scheduleService.getSchedule(getCurrentUserId());
	}

	private Long getCurrentUserId() {
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
	}
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
