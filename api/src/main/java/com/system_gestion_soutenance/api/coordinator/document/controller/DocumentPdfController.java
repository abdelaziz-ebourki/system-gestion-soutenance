package com.system_gestion_soutenance.api.coordinator.document.controller;

import com.system_gestion_soutenance.api.coordinator.document.dto.AttendanceListResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.DefenseIdsRequest;
import com.system_gestion_soutenance.api.coordinator.document.dto.EvaluationSheetResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.JuryConvocationResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.MinutesResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.ProjectIdRequest;
import com.system_gestion_soutenance.api.coordinator.document.dto.ScheduleDocResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.SessionRequest;
import com.system_gestion_soutenance.api.coordinator.document.service.DocumentDataService;
import com.system_gestion_soutenance.api.common.service.PdfGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coordinator/documents/pdf")
@Tag(name = "Coordinator - PDF Documents", description = "Endpoints for generating PDF documents")
public class DocumentPdfController {

	private final DocumentDataService documentDataService;
	private final PdfGenerationService pdfGenerationService;

	public DocumentPdfController(DocumentDataService documentDataService, PdfGenerationService pdfGenerationService) {
		this.documentDataService = documentDataService;
		this.pdfGenerationService = pdfGenerationService;
	}

	@PostMapping("/evaluation-sheets")
	@Operation(summary = "Generate evaluation sheets PDF for a project")
	public ResponseEntity<byte[]> evaluationSheets(@Valid @RequestBody ProjectIdRequest request) {
		List<EvaluationSheetResponse> data = documentDataService
				.evaluationSheets(new DefenseIdsRequest(null, request.projectId()));

		Map<String, Object> templateData = Map.of("sheets", data);
		byte[] pdf = pdfGenerationService.generatePdf("evaluation-sheet", templateData);

		return createPdfResponse("evaluation-sheets.pdf", pdf);
	}

	@PostMapping("/attendance-lists")
	@Operation(summary = "Generate attendance list PDF")
	public ResponseEntity<byte[]> attendanceList(@Valid @RequestBody SessionRequest request) {
		AttendanceListResponse data = documentDataService.attendanceList(request.defenseSessionId());

		Map<String, Object> templateData = Map.of("attendance", data);
		byte[] pdf = pdfGenerationService.generatePdf("attendance-list", templateData);

		return createPdfResponse("attendance-list.pdf", pdf);
	}

	@PostMapping("/jury-convocations")
	@Operation(summary = "Generate jury convocations PDF for a project")
	public ResponseEntity<byte[]> juryConvocations(@Valid @RequestBody ProjectIdRequest request) {
		List<JuryConvocationResponse> data = documentDataService
				.juryConvocations(new DefenseIdsRequest(null, request.projectId()));

		Map<String, Object> templateData = Map.of("convocations", data);
		byte[] pdf = pdfGenerationService.generatePdf("jury-convocation", templateData);

		return createPdfResponse("jury-convocations.pdf", pdf);
	}

	@PostMapping("/schedule")
	@Operation(summary = "Generate printable schedule PDF")
	public ResponseEntity<byte[]> schedule(@Valid @RequestBody SessionRequest request) {
		ScheduleDocResponse data = documentDataService.schedule(request.defenseSessionId());

		Map<String, Object> templateData = Map.of("schedule", data);
		byte[] pdf = pdfGenerationService.generatePdf("schedule", templateData);

		return createPdfResponse("schedule.pdf", pdf);
	}

	@PostMapping("/proces-verbal")
	@Operation(summary = "Generate proces-verbal (PV) PDF for a project")
	public ResponseEntity<byte[]> minutesPdf(@Valid @RequestBody ProjectIdRequest request) {
		MinutesResponse data = documentDataService.minutes(request.projectId());

		Map<String, Object> templateData = Map.of("pv", data);
		byte[] pdf = pdfGenerationService.generatePdf("proces-verbal", templateData);

		return createPdfResponse("proces-verbal.pdf", pdf);
	}

	private ResponseEntity<byte[]> createPdfResponse(String filename, byte[] content) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("filename", filename);
		return new ResponseEntity<>(content, headers, HttpStatus.OK);
	}
}
