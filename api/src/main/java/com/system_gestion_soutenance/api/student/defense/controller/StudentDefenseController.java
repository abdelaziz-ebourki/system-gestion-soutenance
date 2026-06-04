package com.system_gestion_soutenance.api.student.defense.controller;

import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse;
import com.system_gestion_soutenance.api.student.defense.service.StudentDefenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/defense")
@Tag(name = "Student - Defense", description = "Informations sur la soutenance")
public class StudentDefenseController {

	private final StudentDefenseService studentDefenseService;
	private final SecurityService securityService;

	public StudentDefenseController(StudentDefenseService studentDefenseService, SecurityService securityService) {
		this.studentDefenseService = studentDefenseService;
		this.securityService = securityService;
	}

	@GetMapping
	@Operation(summary = "Get the connected student's defense info (project, jury, schedule, status)")
	public StudentDefenseResponse getDefense() {
		return studentDefenseService.getDefense(securityService.getCurrentUserId());
	}
}
