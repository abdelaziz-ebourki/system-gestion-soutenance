package com.system_gestion_soutenance.api.coordinator.document.controller;

import com.system_gestion_soutenance.api.coordinator.document.dto.*;
import com.system_gestion_soutenance.api.coordinator.document.service.DocumentDataService;
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
@Tag(name = "Coordinator - Documents", description = "Données pour les documents PDF (fiches d'évaluation, listes de présence, convocations, planning)")
public class DocumentDataController {

	private final DocumentDataService documentDataService;

	public DocumentDataController(DocumentDataService documentDataService) {
		this.documentDataService = documentDataService;
	}

	@PostMapping("/evaluation-sheets")
	@Operation(summary = "Get evaluation sheets data for a project")
	public ResponseEntity<List<EvaluationSheetResponse>> evaluationSheets(
			@Valid @RequestBody ProjectIdRequest request) {
		DefenseIdsRequest idsRequest = new DefenseIdsRequest(null, request.projectId());
		return ResponseEntity.ok(documentDataService.evaluationSheets(idsRequest));
	}

	@PostMapping("/attendance-lists")
	@Operation(summary = "Get attendance lists data")
	public ResponseEntity<AttendanceListResponse> attendanceList(@Valid @RequestBody SessionRequest request) {
		return ResponseEntity.ok(documentDataService.attendanceList(request.defenseSessionId()));
	}

	@PostMapping("/jury-convocations")
	@Operation(summary = "Get jury convocation data for a project")
	public ResponseEntity<List<JuryConvocationResponse>> juryConvocations(
			@Valid @RequestBody ProjectIdRequest request) {
		DefenseIdsRequest idsRequest = new DefenseIdsRequest(null, request.projectId());
		return ResponseEntity.ok(documentDataService.juryConvocations(idsRequest));
	}

	@PostMapping("/schedule")
	@Operation(summary = "Get printable schedule data")
	public ResponseEntity<ScheduleDocResponse> schedule(@Valid @RequestBody SessionRequest request) {
		return ResponseEntity.ok(documentDataService.schedule(request.defenseSessionId()));
	}

	@PostMapping("/proces-verbal")
	@Operation(summary = "Get proces-verbal (PV) data for a project")
	public ResponseEntity<ProcesVerbalResponse> procesVerbal(@Valid @RequestBody ProjectIdRequest request) {
		return ResponseEntity.ok(documentDataService.procesVerbal(request.projectId()));
	}
}
