package com.system_gestion_soutenance.api.admin.config.document.controller;

import com.system_gestion_soutenance.api.admin.config.document.dto.UpdateDocumentConfigRequest;
import com.system_gestion_soutenance.api.admin.config.document.entity.DocumentConfig;
import com.system_gestion_soutenance.api.admin.config.document.service.DocumentConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/config/documents")
@Tag(name = "Admin - Document Config", description = "Configuration des documents")
public class DocumentConfigController {

	private final DocumentConfigService service;

	public DocumentConfigController(DocumentConfigService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Get document configuration")
	public DocumentConfig get() {
		return service.get();
	}

	@PutMapping
	@Operation(summary = "Update document configuration")
	public DocumentConfig update(@Valid @RequestBody UpdateDocumentConfigRequest updates) {
		return service.update(updates);
	}
}
