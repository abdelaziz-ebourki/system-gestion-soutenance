package com.system_gestion_soutenance.api.teacher.schedule.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.teacher.schedule.dto.TeacherScheduleResponse;
import com.system_gestion_soutenance.api.teacher.schedule.service.TeacherScheduleService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/teacher/schedules")
@PreAuthorize("hasRole('TEACHER')")
@Tag(name = "Teacher - Schedule View", description = "Endpoints for teachers to view their defense schedule")
public class TeacherScheduleController {

	private final TeacherScheduleService scheduleService;

	public TeacherScheduleController(TeacherScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	@GetMapping
	@Operation(summary = "Get defense schedule", description = "Retrieves the defense schedule for the connected teacher.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved schedule")})
	public ApiResponse<TeacherScheduleResponse> get(@AuthenticationPrincipal User user) {
		return ApiResponse.success(scheduleService.getSchedule(user.getId()));
	}
}