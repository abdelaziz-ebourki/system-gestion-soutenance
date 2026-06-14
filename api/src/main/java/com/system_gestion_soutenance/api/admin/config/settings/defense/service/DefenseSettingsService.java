package com.system_gestion_soutenance.api.admin.config.settings.defense.service;

import com.system_gestion_soutenance.api.admin.config.settings.defense.dto.PatchDefenseSettingsRequest;
import com.system_gestion_soutenance.api.admin.config.settings.defense.dto.UpdateDefenseSettingsRequest;
import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.repository.DefenseSettingsRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

	@Audited(action = "UPDATE", entity = "DefenseSettings")
	@Transactional
	public DefenseSettings patch(PatchDefenseSettingsRequest updates) {
		DefenseSettings settings = repository.findById(1L)
				.orElseThrow(() -> new EntityNotFoundException("Paramètres de soutenance non configurés"));

		if (updates.startTime() != null) {
			settings.setStartTime(updates.startTime());
		}
		if (updates.endTime() != null) {
			settings.setEndTime(updates.endTime());
		}
		if (updates.defenseDuration() != null) {
			settings.setDefenseDuration(updates.defenseDuration());
		}
		if (updates.breakDuration() != null) {
			settings.setBreakDuration(updates.breakDuration());
		}
		if (updates.groupCreationStartDate() != null) {
			settings.setGroupCreationStartDate(updates.groupCreationStartDate());
		}
		if (updates.groupCreationEndDate() != null) {
			settings.setGroupCreationEndDate(updates.groupCreationEndDate());
		}

		return repository.save(settings);
	}
}