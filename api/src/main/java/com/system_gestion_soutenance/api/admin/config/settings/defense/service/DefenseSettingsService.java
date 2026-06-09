package com.system_gestion_soutenance.api.admin.config.settings.defense.service;

import com.system_gestion_soutenance.api.admin.config.settings.defense.dto.UpdateDefenseSettingsRequest;
import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.repository.DefenseSettingsRepository;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
@SuppressWarnings("PMD")

@Service
public class DefenseSettingsService {

	private final DefenseSettingsRepository repository;

	public DefenseSettingsService(DefenseSettingsRepository repository) {
		this.repository = repository;
	}

	public DefenseSettings get() {
		return repository.findById(1L)
				.orElseThrow(() -> new EntityNotFoundException("Paramètres de soutenance non configurés"));
	}

	public DefenseSettings update(UpdateDefenseSettingsRequest updates) {
		DefenseSettings settings = repository.findById(1L).orElse(new DefenseSettings());

		settings.setStartTime(updates.startTime());
		settings.setEndTime(updates.endTime());
		settings.setDefenseDuration(updates.defenseDuration());
		settings.setBreakDuration(updates.breakDuration());
		settings.setGroupCreationStartDate(updates.groupCreationStartDate());
		settings.setGroupCreationEndDate(updates.groupCreationEndDate());

		settings.setId(1L);
		return repository.save(settings);
	}
}