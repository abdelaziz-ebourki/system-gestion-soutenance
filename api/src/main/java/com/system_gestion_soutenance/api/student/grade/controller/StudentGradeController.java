package com.system_gestion_soutenance.api.student.grade.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.service.PdfGenerationService;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.student.grade.dto.StudentCertificateResponse;
import com.system_gestion_soutenance.api.student.grade.dto.StudentGradeResponse;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/student/grade")
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student - Grades", description = "Endpoints for students to view their grades and certificates")
public class StudentGradeController {

	private final DefenseRepository defenseRepository;
	private final GroupRepository groupRepository;
	private final SecurityService securityService;
	private final PdfGenerationService pdfGenerationService;

	public StudentGradeController(DefenseRepository defenseRepository, GroupRepository groupRepository,
			SecurityService securityService, PdfGenerationService pdfGenerationService) {
		this.defenseRepository = defenseRepository;
		this.groupRepository = groupRepository;
		this.securityService = securityService;
		this.pdfGenerationService = pdfGenerationService;
	}

	@GetMapping
	@Operation(summary = "Get my grade", description = "Retrieves the grade for the connected student's project if results are published.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Grade retrieved"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No grade found")})
	public ApiResponse<StudentGradeResponse> getMyGrade() {
		Long studentId = securityService.getCurrentUserId();
		Group group = groupRepository.findFirstByStudentsIdOrderByIdAsc(studentId)
				.orElseThrow(() -> new EntityNotFoundException("Aucun groupe trouvé"));

		if (group.getProject() == null) {
			throw new EntityNotFoundException("Aucun projet assigné à votre groupe");
		}

		Defense defense = defenseRepository.findByProject(group.getProject())
				.orElseThrow(() -> new EntityNotFoundException("Aucune soutenance trouvée"));

		if (defense.getFinalScore() == null) {
			return ApiResponse.success(new StudentGradeResponse(group.getProject().getId(),
					group.getProject().getTitle(), null, null, null, "pending", List.of()));
		}

		String status = "published";
		List<String> juryScores = defense.getMembers().stream()
				.map(m -> m.getRoleName() + ": " + m.getTeacher().getFirstName() + " " + m.getTeacher().getLastName())
				.toList();

		return ApiResponse.success(new StudentGradeResponse(group.getProject().getId(), group.getProject().getTitle(),
				defense.getDate() != null ? defense.getDate().toString() : null, defense.getFinalScore(),
				defense.getMention(), status, juryScores));
	}

	@GetMapping("/certificate")
	@Operation(summary = "Download grade certificate", description = "Generates and downloads a PDF certificate for the student's grade.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Certificate generated"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No grade found")})
	public ResponseEntity<byte[]> getCertificate() {
		Long studentId = securityService.getCurrentUserId();
		Group group = groupRepository.findFirstByStudentsIdOrderByIdAsc(studentId)
				.orElseThrow(() -> new EntityNotFoundException("Aucun groupe trouvé"));

		if (group.getProject() == null) {
			throw new EntityNotFoundException("Aucun projet assigné à votre groupe");
		}

		Defense defense = defenseRepository.findByProject(group.getProject())
				.orElseThrow(() -> new EntityNotFoundException("Aucune soutenance trouvée"));

		if (defense.getFinalScore() == null) {
			throw new EntityNotFoundException("Les notes ne sont pas encore disponibles");
		}

		List<String> juryNames = defense.getMembers().stream()
				.map(m -> m.getTeacher().getFirstName() + " " + m.getTeacher().getLastName()).toList();

		StudentCertificateResponse cert = new StudentCertificateResponse("Université",
				group.getStudents().stream().filter(s -> s.getId().equals(studentId)).findFirst()
						.map(s -> s.getFirstName() + " " + s.getLastName()).orElse("Étudiant"),
				group.getProject().getTitle(), group.getProject().getDefenseType(),
				defense.getDate() != null ? defense.getDate().toString() : null, defense.getFinalScore(),
				defense.getMention(), juryNames);

		byte[] pdf = pdfGenerationService.generatePdf("certificate", Map.of("certificate", cert));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("filename", "certificat-de-notes.pdf");
		return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
	}
}
