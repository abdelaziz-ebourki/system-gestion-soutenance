package com.system_gestion_soutenance.api.admin.config.document.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.document.dto.UpdateDocumentConfigRequest;
import com.system_gestion_soutenance.api.admin.config.document.entity.DocumentConfig;
import com.system_gestion_soutenance.api.admin.config.document.repository.DocumentConfigRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class DocumentConfigServiceTest {

	@Mock
	private DocumentConfigRepository repository;

	@InjectMocks
	private DocumentConfigService service;

	@Test
	void get_found_returnsConfig() {
		DocumentConfig config = new DocumentConfig(1L, 10, "pdf,doc", 5);
		when(repository.findById(1L)).thenReturn(Optional.of(config));

		DocumentConfig result = service.get();

		assertEquals(10, result.getMaxFileSizeMb());
		assertEquals("pdf,doc", result.getAllowedExtensions());
		assertEquals(5, result.getVersionLimit());
	}

	@Test
	void get_notFound_throws() {
		when(repository.findById(1L)).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class, () -> service.get());
	}

	@Test
	void update_createNew_whenNotFound() {
		when(repository.findById(1L)).thenReturn(Optional.empty());
		when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateDocumentConfigRequest req = new UpdateDocumentConfigRequest(20, "pdf", 3);
		DocumentConfig result = service.update(req);

		assertEquals(20, result.getMaxFileSizeMb());
		assertEquals("pdf", result.getAllowedExtensions());
		assertEquals(3, result.getVersionLimit());
		assertEquals(1L, result.getId());
	}

	@Test
	void update_existing_updatesFields() {
		DocumentConfig existing = new DocumentConfig(1L, 10, "pdf,doc", 5);
		when(repository.findById(1L)).thenReturn(Optional.of(existing));
		when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateDocumentConfigRequest req = new UpdateDocumentConfigRequest(30, "pdf,doc,xls", 10);
		DocumentConfig result = service.update(req);

		assertEquals(30, result.getMaxFileSizeMb());
		assertEquals("pdf,doc,xls", result.getAllowedExtensions());
		assertEquals(10, result.getVersionLimit());
	}
}
