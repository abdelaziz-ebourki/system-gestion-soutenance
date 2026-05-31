package com.system_gestion_soutenance.api.admin.audit.controller;

import com.system_gestion_soutenance.api.admin.audit.dto.AuditLogRequest;
import com.system_gestion_soutenance.api.admin.audit.entity.AuditLog;
import com.system_gestion_soutenance.api.admin.audit.service.AuditLogService;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit-logs")
@Tag(name = "Admin - Audit Logs", description = "Journal d'audit")
public class AuditLogController {

	private final AuditLogService service;

	public AuditLogController(AuditLogService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "List audit logs with pagination")
	public PaginatedResponse<AuditLog> findAll(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int limit) {
		return service.getAuditLogs(page, limit);
	}

	@PostMapping
	@Operation(summary = "Create an audit log entry")
	public ResponseEntity<AuditLog> create(@Valid @RequestBody AuditLogRequest request) {
		AuditLog log = new AuditLog();
		log.setAction(request.action());
		log.setEntity(request.entity());
		log.setEntityId(request.entityId());
		String adminEmail = extractAdminEmail();
		log.setAdminEmail(adminEmail != null ? adminEmail : request.adminEmail());
		log.setDetails(request.details());
		log.setTimestamp(LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.CREATED).body(service.save(log));
	}

	private String extractAdminEmail() {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated()) {
			Object principal = auth.getPrincipal();
			if (principal instanceof User user) {
				return user.getEmail();
			}
			if (principal instanceof String email) {
				return email;
			}
		}
		return null;
	}
}
