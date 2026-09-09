package com.system_gestion_soutenance.api.admin.audit.service;

import com.system_gestion_soutenance.api.admin.audit.entity.AuditLog;
import com.system_gestion_soutenance.api.admin.audit.repository.AuditLogRepository;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
@Transactional(readOnly = true)
public class AuditLogService {

	private final AuditLogRepository repository;

	public AuditLogService(AuditLogRepository repository) {
		this.repository = repository;
	}

	public PaginatedResponse<AuditLog> getAuditLogs(int page, int limit) {
		PageRequest pageable = PageRequest.of(page, limit);
		Page<AuditLog> auditLogPage = repository.findAllByOrderByTimestampDesc(pageable);

		return new PaginatedResponse<>(auditLogPage.getContent(), auditLogPage.getTotalElements(),
				auditLogPage.getTotalPages(), auditLogPage.getNumber(), auditLogPage.getSize());
	}

	@Transactional
	public AuditLog save(AuditLog log) {
		return repository.save(log);
	}
}