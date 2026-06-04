package com.system_gestion_soutenance.api.student.convocation.controller;

import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse;
import com.system_gestion_soutenance.api.student.defense.service.StudentDefenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/student/convocation")
@Tag(name = "Student - Convocation", description = "Génération de la convocation PDF")
public class ConvocationController {

	private final StudentDefenseService studentDefenseService;
	private final SecurityService securityService;

	public ConvocationController(StudentDefenseService studentDefenseService, SecurityService securityService) {
		this.studentDefenseService = studentDefenseService;
		this.securityService = securityService;
	}

	@GetMapping
	@Operation(summary = "Get the convocation PDF for the connected student")
	public ResponseEntity<byte[]> getConvocation() {
		Long studentId = securityService.getCurrentUserId();

		StudentDefenseResponse defense;
		try {
			defense = studentDefenseService.getDefense(studentId);
		} catch (ResponseStatusException e) {
			return ResponseEntity.notFound().build();
		}

		if (!"scheduled".equals(defense.status())) {
			return ResponseEntity.notFound().build();
		}

		String placeholder = "Convocation pour l'étudiant: " + studentId
				+ "\n\nCe document est un placeholder en attendant la génération PDF.";
		byte[] content = placeholder.getBytes(java.nio.charset.StandardCharsets.UTF_8);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("filename", "convocation.pdf");

		return new ResponseEntity<>(content, headers, HttpStatus.OK);
	}
}
