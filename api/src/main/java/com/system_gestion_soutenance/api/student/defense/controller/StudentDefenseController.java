package com.system_gestion_soutenance.api.student.defense.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse;
import com.system_gestion_soutenance.api.student.defense.service.StudentDefenseService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/defense")
@Tag(name = "Student - Defense", description = "Informations sur la soutenance")
public class StudentDefenseController {

	private final StudentDefenseService studentDefenseService;

	public StudentDefenseController(StudentDefenseService studentDefenseService) {
		this.studentDefenseService = studentDefenseService;
	}

	@GetMapping
	@Operation(summary = "Get the connected student's defense info (project, jury, schedule, status)")
	public ApiResponse<StudentDefenseResponse> getDefense(@AuthenticationPrincipal User user) {
		return ApiResponse.success(studentDefenseService.getDefense(user.getId()));
	}
}
