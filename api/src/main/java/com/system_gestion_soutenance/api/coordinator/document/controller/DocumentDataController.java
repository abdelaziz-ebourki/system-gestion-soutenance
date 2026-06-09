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
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coordinator/documents")
@Tag(name = "Coordinator - Documents", description = "PDF Document Data (Evaluation Sheets, Attendance Lists, Convocations, Schedule)")
public class DocumentDataController {

	private final DocumentDataService documentDataService;

	public DocumentDataController(DocumentDataService documentDataService) {
		this.documentDataService = documentDataService;
	}

	@PostMapping("/evaluation-sheets")
	@Operation(summary = "Get evaluation sheets data for a project")
	public ResponseEntity<ApiResponse<List<EvaluationSheetResponse>>> evaluationSheets(
			@Valid @RequestBody ProjectIdRequest request) {
		DefenseIdsRequest idsRequest = new DefenseIdsRequest(null, request.projectId());
		return ResponseEntity.ok(ApiResponse.success("Données des fiches d'évaluation récupérées avec succès",
				documentDataService.evaluationSheets(idsRequest)));
	}

	@PostMapping("/attendance-lists")
	@Operation(summary = "Get attendance lists data")
	public ResponseEntity<ApiResponse<AttendanceListResponse>> attendanceList(
			@Valid @RequestBody SessionRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Données de la liste de présence récupérées avec succès",
				documentDataService.attendanceList(request.defenseSessionId())));
	}

	@PostMapping("/jury-convocations")
	@Operation(summary = "Get jury convocation data for a project")
	public ResponseEntity<ApiResponse<List<JuryConvocationResponse>>> juryConvocations(
			@Valid @RequestBody ProjectIdRequest request) {
		DefenseIdsRequest idsRequest = new DefenseIdsRequest(null, request.projectId());
		return ResponseEntity.ok(ApiResponse.success("Données des convocations récupérées avec succès",
				documentDataService.juryConvocations(idsRequest)));
	}

	@PostMapping("/schedule")
	@Operation(summary = "Get printable schedule data")
	public ResponseEntity<ApiResponse<ScheduleDocResponse>> schedule(@Valid @RequestBody SessionRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Données du planning récupérées avec succès",
				documentDataService.schedule(request.defenseSessionId())));
	}

	@PostMapping("/proces-verbal")
	@Operation(summary = "Get proces-verbal (PV) data for a project")
	public ResponseEntity<ApiResponse<MinutesResponse>> minutes(@Valid @RequestBody ProjectIdRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Données du procès-verbal récupérées avec succès",
				documentDataService.minutes(request.projectId())));
	}
}
