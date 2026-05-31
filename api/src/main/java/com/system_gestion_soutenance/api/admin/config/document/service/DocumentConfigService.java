package com.system_gestion_soutenance.api.admin.config.document.service;

import com.system_gestion_soutenance.api.admin.config.document.dto.UpdateDocumentConfigRequest;
import com.system_gestion_soutenance.api.admin.config.document.entity.DocumentConfig;
import com.system_gestion_soutenance.api.admin.config.document.repository.DocumentConfigRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentConfigService {

	private final DocumentConfigRepository repository;

	public DocumentConfigService(DocumentConfigRepository repository) {
		this.repository = repository;
	}

	public DocumentConfig get() {
		return repository.findById(1L).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Configuration des documents non trouvée"));
	}

	public DocumentConfig update(UpdateDocumentConfigRequest updates) {
		DocumentConfig config = repository.findById(1L).orElse(new DocumentConfig());

		config.setMaxFileSizeMb(updates.maxFileSizeMb());
		config.setAllowedExtensions(updates.allowedExtensions());
		config.setVersionLimit(updates.versionLimit());

		config.setId(1L);
		return repository.save(config);
	}
}
