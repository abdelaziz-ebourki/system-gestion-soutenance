package com.system_gestion_soutenance.api.admin.audit.repository;

import com.system_gestion_soutenance.api.admin.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
	Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
