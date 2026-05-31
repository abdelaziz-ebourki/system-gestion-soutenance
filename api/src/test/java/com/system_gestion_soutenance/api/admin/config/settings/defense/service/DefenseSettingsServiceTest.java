package com.system_gestion_soutenance.api.admin.config.settings.defense.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.settings.defense.dto.UpdateDefenseSettingsRequest;
import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.repository.DefenseSettingsRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DefenseSettingsServiceTest {

	@Mock
	private DefenseSettingsRepository repository;

	@InjectMocks
	private DefenseSettingsService service;

	@Test
	void get_found_returnsSettings() {
		DefenseSettings settings = new DefenseSettings(1L, "08:00", "18:00", 30, 15, null, null);
		when(repository.findById(1L)).thenReturn(Optional.of(settings));

		DefenseSettings result = service.get();

		assertEquals("08:00", result.getStartTime());
		assertEquals("18:00", result.getEndTime());
		assertEquals(30, result.getDefenseDuration());
		assertEquals(15, result.getBreakDuration());
	}

	@Test
	void get_notFound_throws() {
		when(repository.findById(1L)).thenReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> service.get());
	}

	@Test
	void update_createNew_whenNotFound() {
		when(repository.findById(1L)).thenReturn(Optional.empty());
		when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateDefenseSettingsRequest req = new UpdateDefenseSettingsRequest("09:00", "17:00", 45, 10, "2026-06-01",
				"2026-06-30");
		DefenseSettings result = service.update(req);

		assertEquals("09:00", result.getStartTime());
		assertEquals("17:00", result.getEndTime());
		assertEquals(45, result.getDefenseDuration());
		assertEquals(10, result.getBreakDuration());
		assertEquals("2026-06-01", result.getGroupCreationStartDate());
		assertEquals("2026-06-30", result.getGroupCreationEndDate());
		assertEquals(1L, result.getId());
	}

	@Test
	void update_existing_updatesFields() {
		DefenseSettings existing = new DefenseSettings(1L, "08:00", "18:00", 30, 15, null, null);
		when(repository.findById(1L)).thenReturn(Optional.of(existing));
		when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateDefenseSettingsRequest req = new UpdateDefenseSettingsRequest("10:00", "16:00", 20, 5, null, null);
		DefenseSettings result = service.update(req);

		assertEquals("10:00", result.getStartTime());
		assertEquals("16:00", result.getEndTime());
		assertEquals(20, result.getDefenseDuration());
	}
}
