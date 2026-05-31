package com.system_gestion_soutenance.api.admin.audit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.audit.entity.AuditLog;
import com.system_gestion_soutenance.api.admin.audit.repository.AuditLogRepository;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

	@Mock
	private AuditLogRepository repository;
	@InjectMocks
	private AuditLogService service;

	@Test
	void getAuditLogs_returnsPaginated() {
		AuditLog log = new AuditLog();
		Page<AuditLog> page = new PageImpl<>(List.of(log));
		when(repository.findAllByOrderByTimestampDesc(any(PageRequest.class))).thenReturn(page);

		PaginatedResponse<AuditLog> result = service.getAuditLogs(0, 20);

		assertEquals(1, result.items().size());
		assertEquals(1, result.total());
	}

	@Test
	void save_returnsSaved() {
		AuditLog log = new AuditLog();
		when(repository.save(any())).thenReturn(log);

		AuditLog result = service.save(log);
		assertNotNull(result);
		verify(repository).save(log);
	}
}
