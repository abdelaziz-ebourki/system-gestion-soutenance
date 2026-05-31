package com.system_gestion_soutenance.api.admin.config.email.service;

import com.system_gestion_soutenance.api.admin.config.email.dto.UpdateEmailConfigRequest;
import com.system_gestion_soutenance.api.admin.config.email.entity.EmailConfig;
import com.system_gestion_soutenance.api.admin.config.email.repository.EmailConfigRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmailConfigService {

	private final EmailConfigRepository repository;

	public EmailConfigService(EmailConfigRepository repository) {
		this.repository = repository;
	}

	public EmailConfig get() {
		return repository.findById(1L).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Configuration email non trouvée"));
	}

	public EmailConfig update(UpdateEmailConfigRequest updates) {
		EmailConfig config = repository.findById(1L).orElse(new EmailConfig());

		config.setHost(updates.host());
		config.setPort(updates.port());
		config.setUsername(updates.username());
		if (updates.password() != null)
			config.setPassword(updates.password());
		config.setSenderName(updates.senderName());
		config.setSenderEmail(updates.senderEmail());
		config.setEncryption(updates.encryption());

		config.setId(1L);
		return repository.save(config);
	}
}
