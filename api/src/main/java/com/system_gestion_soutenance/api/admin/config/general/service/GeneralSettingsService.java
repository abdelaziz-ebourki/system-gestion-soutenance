package com.system_gestion_soutenance.api.admin.config.general.service;

import com.system_gestion_soutenance.api.admin.config.general.dto.UpdateGeneralSettingsRequest;
import com.system_gestion_soutenance.api.admin.config.general.entity.GeneralSettings;
import com.system_gestion_soutenance.api.admin.config.general.repository.GeneralSettingsRepository;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
@SuppressWarnings("PMD")

@Service
public class GeneralSettingsService {

	private final GeneralSettingsRepository repository;

	public GeneralSettingsService(GeneralSettingsRepository repository) {
		this.repository = repository;
	}

	public GeneralSettings get() {
		return repository.findById(1L)
				.orElseThrow(() -> new EntityNotFoundException("Paramètres généraux non configurés"));
	}

	public GeneralSettings update(UpdateGeneralSettingsRequest updates) {
		GeneralSettings settings = repository.findById(1L).orElse(new GeneralSettings());

		settings.setInstitutionName(updates.institutionName());
		settings.setInstitutionLogoUrl(updates.institutionLogoUrl());
		settings.setTimezone(updates.timezone());
		settings.setDateFormat(updates.dateFormat());
		if (updates.setupCompleted() != null)
			settings.setSetupCompleted(updates.setupCompleted());

		settings.setId(1L);
		return repository.save(settings);
	}
}