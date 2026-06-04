package com.system_gestion_soutenance.api.teacher.schedule.controller;

import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.teacher.schedule.dto.TeacherScheduleResponse;
import com.system_gestion_soutenance.api.teacher.schedule.service.TeacherScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/schedule")
@Tag(name = "Teacher - Schedule", description = "Planning des soutenances pour l'enseignant")
public class TeacherScheduleController {

	private final TeacherScheduleService scheduleService;
	private final SecurityService securityService;

	public TeacherScheduleController(TeacherScheduleService scheduleService, SecurityService securityService) {
		this.scheduleService = scheduleService;
		this.securityService = securityService;
	}

	@GetMapping
	@Operation(summary = "Get the connected teacher's defense schedule")
	public TeacherScheduleResponse get() {
		return scheduleService.getSchedule(securityService.getCurrentUserId());
	}
}
