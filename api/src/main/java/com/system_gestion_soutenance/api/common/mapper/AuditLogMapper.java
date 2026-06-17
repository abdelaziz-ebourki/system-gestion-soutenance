package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.admin.audit.entity.AuditLog;
import com.system_gestion_soutenance.api.admin.audit.dto.AuditLogDto;
import org.mapstruct.Mapper;
@SuppressWarnings("PMD")

@Mapper(config = CentralMapperConfig.class)
public interface AuditLogMapper {
	AuditLogDto toDto(AuditLog auditLog);
}