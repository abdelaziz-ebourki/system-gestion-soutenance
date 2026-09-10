package com.system_gestion_soutenance.api.coordinator.report.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.service.PdfGenerationService;
import com.system_gestion_soutenance.api.coordinator.report.dto.GradeHistoryResponse;
import com.system_gestion_soutenance.api.coordinator.report.dto.SessionReportResponse;
import com.system_gestion_soutenance.api.coordinator.report.dto.TeacherWorkloadResponse;
import com.system_gestion_soutenance.api.coordinator.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator")
@Tag(name = "Coordinator - Reports", description = "Endpoints for session reports, teacher workload, and grade history")
public class ReportController {

	private final ReportService reportService;
	private final PdfGenerationService pdfGenerationService;

	public ReportController(ReportService reportService, PdfGenerationService pdfGenerationService) {
		this.reportService = reportService;
		this.pdfGenerationService = pdfGenerationService;
	}

	@GetMapping("/sessions/{id}/report")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Generate session report PDF", description = "Generates a PDF report (PV de session) for a defense session.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "PDF generated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")})
	public ResponseEntity<byte[]> getSessionReport(@Parameter(description = "Session ID") @PathVariable Long id) {
		SessionReportResponse data = reportService.getSessionReport(id);
		byte[] pdf = pdfGenerationService.generatePdf("session-report", Map.of("report", data));
		reportService.recordDocumentGeneration("SESSION_REPORT", id, (long) pdf.length);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("filename", "rapport-session-" + id + ".pdf");
		return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
	}

	@GetMapping("/reports/teacher-workload")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Get teacher workload", description = "Returns the workload of all teachers (supervisions + jury).")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workload data retrieved")})
	public ApiResponse<List<TeacherWorkloadResponse>> getTeacherWorkload() {
		return ApiResponse.success(reportService.getTeacherWorkload());
	}

	@GetMapping("/reports/grade-history/{sessionId}")
	@PreAuthorize("hasRole('COORDINATOR')")
	@Operation(summary = "Get grade history", description = "Returns the complete grade history for a session.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Grade history retrieved"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")})
	public ApiResponse<List<GradeHistoryResponse>> getGradeHistory(
			@Parameter(description = "Session ID") @PathVariable Long sessionId) {
		return ApiResponse.success(reportService.getGradeHistory(sessionId));
	}
}
