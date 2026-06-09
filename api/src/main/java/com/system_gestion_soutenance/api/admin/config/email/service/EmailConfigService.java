package com.system_gestion_soutenance.api.admin.config.email.service;

import com.system_gestion_soutenance.api.admin.config.email.dto.UpdateEmailConfigRequest;
import com.system_gestion_soutenance.api.admin.config.email.entity.EmailConfig;
import com.system_gestion_soutenance.api.admin.config.email.repository.EmailConfigRepository;
import com.system_gestion_soutenance.api.common.util.EncryptionUtil;
import com.system_gestion_soutenance.api.notification.service.EmailService;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
public class EmailConfigService {

	private final EmailConfigRepository repository;
	private final EncryptionUtil encryptionUtil;
	private final EmailService emailService;

	public EmailConfigService(EmailConfigRepository repository, EncryptionUtil encryptionUtil,
			EmailService emailService) {
		this.repository = repository;
		this.encryptionUtil = encryptionUtil;
		this.emailService = emailService;
	}

	public EmailConfig get() {
		return repository.findById(1L)
				.orElseThrow(() -> new EntityNotFoundException("Configuration email non trouvée"));
	}

	@Transactional
	public EmailConfig update(UpdateEmailConfigRequest updates) {
		EmailConfig config = repository.findById(1L).orElse(new EmailConfig());

		config.setHost(updates.host());
		config.setPort(updates.port());
		config.setUsername(updates.username());
		if (updates.password() != null)
			config.setPassword(encryptionUtil.encrypt(updates.password()));
		config.setSenderName(updates.senderName());
		config.setSenderEmail(updates.senderEmail());
		config.setEncryption(updates.encryption());

		config.setId(1L);
		EmailConfig saved = repository.save(config);
		emailService.reconfigure();
		return saved;
	}
}