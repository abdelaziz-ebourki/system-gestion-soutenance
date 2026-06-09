package com.system_gestion_soutenance.api.coordinator.document.controller;

import com.system_gestion_soutenance.api.common.pdf.DocumentGenerationService;
import com.system_gestion_soutenance.api.coordinator.document.dto.AttendanceListResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.DefenseIdsRequest;
import com.system_gestion_soutenance.api.coordinator.document.dto.EvaluationSheetResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.JuryConvocationResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.MinutesResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.ProjectIdRequest;
import com.system_gestion_soutenance.api.coordinator.document.dto.ScheduleDocResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.SessionRequest;
import com.system_gestion_soutenance.api.coordinator.document.service.DocumentDataService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator/documents")
@Tag(name = "Coordinator - Documents", description = "PDF Document Data (Evaluation Sheets, Attendance Lists, Convocations, Schedule)")
public class DocumentDataController {

	private final DocumentDataService documentDataService;
	private final DocumentGenerationService documentGenerationService;

	public DocumentDataController(DocumentDataService documentDataService,
			DocumentGenerationService documentGenerationService) {
		this.documentDataService = documentDataService;
		this.documentGenerationService = documentGenerationService;
	}

	@PostMapping("/evaluation-sheets")
	@Operation(summary = "Get evaluation sheets data for a project")
	public ResponseEntity<ApiResponse<List<EvaluationSheetResponse>>> evaluationSheets(
			@Valid @RequestBody ProjectIdRequest request) {
		DefenseIdsRequest idsRequest = new DefenseIdsRequest(null, request.projectId());
		return ResponseEntity.ok(ApiResponse.success("Donnees des fiches d'evaluation recuperees avec succes",
				documentDataService.evaluationSheets(idsRequest)));
	}

	@PostMapping("/evaluation-sheets/pdf")
	@Operation(summary = "Download evaluation sheet PDF for a project")
	public ResponseEntity<byte[]> evaluationSheetsPdf(@Valid @RequestBody ProjectIdRequest request) {
		DefenseIdsRequest idsRequest = new DefenseIdsRequest(null, request.projectId());
		List<EvaluationSheetResponse> sheets = documentDataService.evaluationSheets(idsRequest);
		if (sheets.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		EvaluationSheetResponse s = sheets.get(0);
		Map<String, Object> data = Map.of("projectTitle", s.projectTitle() != null ? s.projectTitle() : "",
				"studentNames", s.studentNames() != null ? s.studentNames() : List.of(), "supervisorName",
				s.supervisorName() != null ? s.supervisorName() : "", "date", s.date() != null ? s.date() : "", "time",
				s.time() != null ? s.time() : "", "room", s.roomName() != null ? s.roomName() : "", "juryMembers",
				s.juryMembers() != null ? s.juryMembers() : List.of());
		byte[] pdf = documentGenerationService.generatePdf("evaluation-sheet", data);
		return pdfResponse(pdf, "fiche-evaluation-" + request.projectId() + ".pdf");
	}

	@PostMapping("/attendance-lists")
	@Operation(summary = "Get attendance lists data")
	public ResponseEntity<ApiResponse<AttendanceListResponse>> attendanceList(
			@Valid @RequestBody SessionRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Donnees de la liste de presence recuperees avec succes",
				documentDataService.attendanceList(request.defenseSessionId())));
	}

	@PostMapping("/attendance-lists/pdf")
	@Operation(summary = "Download attendance list PDF")
	public ResponseEntity<byte[]> attendanceListPdf(@Valid @RequestBody SessionRequest request) {
		AttendanceListResponse attendance = documentDataService.attendanceList(request.defenseSessionId());
		Map<String, Object> data = Map.of("sessionName",
				attendance.defenseSessionName() != null ? attendance.defenseSessionName() : "", "slots",
				attendance.slots() != null ? attendance.slots() : List.of());
		byte[] pdf = documentGenerationService.generatePdf("attendance-list", data);
		return pdfResponse(pdf, "liste-presence-" + request.defenseSessionId() + ".pdf");
	}

	@PostMapping("/jury-convocations")
	@Operation(summary = "Get jury convocation data for a project")
	public ResponseEntity<ApiResponse<List<JuryConvocationResponse>>> juryConvocations(
			@Valid @RequestBody ProjectIdRequest request) {
		DefenseIdsRequest idsRequest = new DefenseIdsRequest(null, request.projectId());
		return ResponseEntity.ok(ApiResponse.success("Donnees des convocations recuperees avec succes",
				documentDataService.juryConvocations(idsRequest)));
	}

	@PostMapping("/jury-convocations/pdf")
	@Operation(summary = "Download jury convocation PDF for a project")
	public ResponseEntity<byte[]> juryConvocationsPdf(@Valid @RequestBody ProjectIdRequest request) {
		DefenseIdsRequest idsRequest = new DefenseIdsRequest(null, request.projectId());
		List<JuryConvocationResponse> convocations = documentDataService.juryConvocations(idsRequest);
		if (convocations.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		JuryConvocationResponse c = convocations.get(0);
		Map<String, Object> data = Map.of("teacherName", c.teacherName() != null ? c.teacherName() : "", "role",
				c.role() != null ? c.role() : "", "projectTitle", c.projectTitle() != null ? c.projectTitle() : "",
				"studentNames", c.studentNames() != null ? c.studentNames() : List.of(), "date",
				c.date() != null ? c.date() : "", "time", c.time() != null ? c.time() : "", "room",
				c.roomName() != null ? c.roomName() : "", "sessionName",
				c.defenseSessionName() != null ? c.defenseSessionName() : "");
		byte[] pdf = documentGenerationService.generatePdf("jury-convocation", data);
		return pdfResponse(pdf, "convocation-jury-" + request.projectId() + ".pdf");
	}

	@PostMapping("/schedule/pdf")
	@Operation(summary = "Download printable schedule PDF")
	public ResponseEntity<byte[]> schedulePdf(@Valid @RequestBody SessionRequest request) {
		ScheduleDocResponse schedule = documentDataService.schedule(request.defenseSessionId());
		Map<String, Object> data = Map.of("sessionName",
				schedule.defenseSessionName() != null ? schedule.defenseSessionName() : "", "slots",
				schedule.slots() != null ? schedule.slots() : List.of());
		byte[] pdf = documentGenerationService.generatePdf("schedule", data);
		return pdfResponse(pdf, "planning-" + request.defenseSessionId() + ".pdf");
	}

	@PostMapping("/schedule")
	@Operation(summary = "Get printable schedule data")
	public ResponseEntity<ApiResponse<ScheduleDocResponse>> schedule(@Valid @RequestBody SessionRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Donnees du planning recuperees avec succes",
				documentDataService.schedule(request.defenseSessionId())));
	}

	@PostMapping("/proces-verbal/pdf")
	@Operation(summary = "Download proces-verbal (PV) PDF for a project")
	public ResponseEntity<byte[]> minutesPdf(@Valid @RequestBody ProjectIdRequest request) {
		MinutesResponse minutes = documentDataService.minutes(request.projectId());
		Map<String, Object> data = Map.of("settings", minutes.settings() != null ? minutes.settings() : "", "grade",
				minutes.grade() != null ? minutes.grade() : "", "studentNames",
				minutes.studentNames() != null ? minutes.studentNames() : List.of(), "supervisorName",
				minutes.supervisorName() != null ? minutes.supervisorName() : "", "juryMembers",
				minutes.juryMembers() != null ? minutes.juryMembers() : List.of());
		byte[] pdf = documentGenerationService.generatePdf("proces-verbal", data);
		return pdfResponse(pdf, "pv-" + request.projectId() + ".pdf");
	}

	@PostMapping("/proces-verbal")
	@Operation(summary = "Get proces-verbal (PV) data for a project")
	public ResponseEntity<ApiResponse<MinutesResponse>> minutes(@Valid @RequestBody ProjectIdRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Donnees du proces-verbal recuperees avec succes",
				documentDataService.minutes(request.projectId())));
	}

	private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment", filename);
		return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
	}
}
