package com.system_gestion_soutenance.api.student.convocation.controller;

import com.system_gestion_soutenance.api.common.pdf.DocumentGenerationService;
import com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse;
import com.system_gestion_soutenance.api.student.defense.service.StudentDefenseService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.system_gestion_soutenance.api.common.exception.BaseBusinessException;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/student/convocations")
@Tag(name = "Student - Convocation Management", description = "Endpoints for generating the convocation PDF")
public class ConvocationController {

	private final StudentDefenseService studentDefenseService;
	private final DocumentGenerationService documentGenerationService;

	public ConvocationController(StudentDefenseService studentDefenseService,
			DocumentGenerationService documentGenerationService) {
		this.studentDefenseService = studentDefenseService;
		this.documentGenerationService = documentGenerationService;
	}

	@GetMapping
	@Operation(summary = "Get convocation PDF", description = "Generates and returns the convocation PDF for the connected student.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully generated PDF"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Convocation not available or student not scheduled")})
	public ResponseEntity<byte[]> getConvocation(@AuthenticationPrincipal User user) {
		Long studentId = user.getId();

		StudentDefenseResponse defense;
		try {
			defense = studentDefenseService.getDefense(studentId);
		} catch (BaseBusinessException e) {
			return ResponseEntity.notFound().build();
		}

		if (!"scheduled".equals(defense.status())) {
			return ResponseEntity.notFound().build();
		}

		Map<String, Object> data = Map.of("studentName", user.getFirstName() + " " + user.getLastName(), "projectTitle",
				defense.projectTitle() != null ? defense.projectTitle() : "", "date",
				defense.date() != null ? defense.date() : "", "time",
				defense.startTime() != null ? defense.startTime() : "", "room",
				defense.roomName() != null ? defense.roomName() : "", "supervisorName",
				defense.supervisorName() != null ? defense.supervisorName() : "", "sessionName", "");

		byte[] content = documentGenerationService.generatePdf("student-convocation", data);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("filename", "convocation.pdf");

		return new ResponseEntity<>(content, headers, HttpStatus.OK);
	}
}
