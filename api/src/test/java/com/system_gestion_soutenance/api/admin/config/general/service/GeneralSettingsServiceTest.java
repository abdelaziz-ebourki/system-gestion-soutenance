package com.system_gestion_soutenance.api.admin.config.general.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.general.dto.UpdateGeneralSettingsRequest;
import com.system_gestion_soutenance.api.admin.config.general.entity.GeneralSettings;
import com.system_gestion_soutenance.api.admin.config.general.repository.GeneralSettingsRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class GeneralSettingsServiceTest {

	@Mock
	private GeneralSettingsRepository repository;

	@InjectMocks
	private GeneralSettingsService service;

	@Test
	void get_withExistingSettings_returnsThem() {
		GeneralSettings settings = new GeneralSettings();
		settings.setId(1L);
		settings.setInstitutionName("Test Univ");
		when(repository.findById(1L)).thenReturn(Optional.of(settings));

		GeneralSettings result = service.get();

		assertEquals("Test Univ", result.getInstitutionName());
	}

	@Test
	void get_withoutSettings_throws404() {
		when(repository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.get());
	}

	@Test
	void update_withExistingSettings_updatesFields() {
		GeneralSettings existing = new GeneralSettings();
		existing.setId(1L);
		existing.setInstitutionName("Old Name");
		when(repository.findById(1L)).thenReturn(Optional.of(existing));
		when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateGeneralSettingsRequest request = new UpdateGeneralSettingsRequest("New Name", "logo.png", "UTC",
				"DD/MM/YYYY", true);

		GeneralSettings result = service.update(request);

		assertEquals("New Name", result.getInstitutionName());
		verify(repository).save(existing);
	}

	@Test
	void update_withNullSetupCompleted_keepsExistingValue() {
		GeneralSettings existing = new GeneralSettings();
		existing.setId(1L);
		existing.setSetupCompleted(true);
		when(repository.findById(1L)).thenReturn(Optional.of(existing));
		when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateGeneralSettingsRequest request = new UpdateGeneralSettingsRequest("Name", null, "UTC", "DD/MM/YYYY",
				null);

		GeneralSettings result = service.update(request);

		assertTrue(result.isSetupCompleted());
	}

	@Test
	void update_withoutExistingSettings_createsNew() {
		when(repository.findById(1L)).thenReturn(Optional.empty());
		when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateGeneralSettingsRequest request = new UpdateGeneralSettingsRequest("New Univ", null, "Africa/Casablanca",
				"YYYY-MM-DD", false);

		GeneralSettings result = service.update(request);

		assertEquals("New Univ", result.getInstitutionName());
		verify(repository).save(argThat(s -> s.getId() == 1L));
	}
}
