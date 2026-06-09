package com.system_gestion_soutenance.api.admin.audit.controller;

import com.system_gestion_soutenance.api.admin.audit.dto.AuditLogRequest;
import com.system_gestion_soutenance.api.admin.audit.dto.AuditLogDto;
import com.system_gestion_soutenance.api.admin.audit.entity.AuditLog;
import com.system_gestion_soutenance.api.admin.audit.service.AuditLogService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.AuditLogMapper;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/admin/audit-logs")
@Tag(name = "Admin - Audit Logging", description = "Endpoints for viewing and creating system audit logs")
public class AuditLogController {

	private final AuditLogService service;
	private final AuditLogMapper mapper;
	private final SecurityService securityService;

	public AuditLogController(AuditLogService service, AuditLogMapper mapper, SecurityService securityService) {
		this.service = service;
		this.mapper = mapper;
		this.securityService = securityService;
	}

	@GetMapping
	@Operation(summary = "List audit logs", description = "Retrieves a paginated list of system audit logs.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved audit logs")})
	public ApiResponse<PaginatedResponse<AuditLogDto>> findAll(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int limit) {
		PaginatedResponse<AuditLog> response = service.getAuditLogs(page, limit);
		List<AuditLogDto> items = response.items().stream().map(mapper::toDto).toList();
		PaginatedResponse<AuditLogDto> mapped = new PaginatedResponse<>(items, response.total(), response.pageCount(),
				page, limit);
		return ApiResponse.success("Liste des logs d'audit récupérée avec succès", mapped);
	}

	@PostMapping
	@Operation(summary = "Create audit log", description = "Manually creates an audit log entry.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Audit log created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid log data")})
	public ResponseEntity<ApiResponse<AuditLogDto>> create(@Valid @RequestBody AuditLogRequest request) {
		AuditLog log = new AuditLog();
		log.setAction(request.action());
		log.setEntity(request.entity());
		log.setEntityId(request.entityId());
		String email = securityService.getOptionalCurrentUserEmail();
		log.setPerformedByEmail(email != null ? email : request.performedByEmail());
		log.setDetails(request.details());
		log.setTimestamp(LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Log d'audit créé avec succès", mapper.toDto(service.save(log))));
	}
}