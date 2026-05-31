package com.system_gestion_soutenance.api.student.defense.controller;

import com.system_gestion_soutenance.api.student.defense.service.StudentDefenseService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
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
	public Map<String, Object> getDefense() {
		return studentDefenseService.getDefense(getCurrentUserId());
	}

	private Long getCurrentUserId() {
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
	}
}
