package com.system_gestion_soutenance.api.admin.config.email.controller;

import com.system_gestion_soutenance.api.admin.config.email.dto.UpdateEmailConfigRequest;
import com.system_gestion_soutenance.api.admin.config.email.entity.EmailConfig;
import com.system_gestion_soutenance.api.admin.config.email.service.EmailConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/config/email")
@Tag(name = "Admin - Email Config", description = "Email Configuration")
public class EmailConfigController {

	private final EmailConfigService service;

	public EmailConfigController(EmailConfigService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Get email configuration")
	public EmailConfig get() {
		return service.get();
	}

	@PutMapping
	@Operation(summary = "Update email configuration")
	public EmailConfig update(@Valid @RequestBody UpdateEmailConfigRequest updates) {
		return service.update(updates);
	}
}
