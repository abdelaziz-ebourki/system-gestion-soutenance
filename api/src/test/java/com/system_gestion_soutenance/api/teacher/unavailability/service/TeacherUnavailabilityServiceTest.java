package com.system_gestion_soutenance.api.teacher.unavailability.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherUnavailabilityServiceTest {

	@Mock
	private UnavailabilityRepository repository;

	@InjectMocks
	private TeacherUnavailabilityService service;

	@SuppressWarnings("unchecked")
	@Test
	void getByTeacher_returnsSlotsByDate() {
		Unavailability ua = new Unavailability(1L, 1L, "2026-06-01", List.of("08:00", "09:00"));
		when(repository.findAll()).thenReturn(List.of(ua));

		Map<String, Object> result = service.getByTeacher(1L);

		Map<String, List<String>> slotsByDate = (Map<String, List<String>>) result.get("slotsByDate");
		assertEquals(1, slotsByDate.size());
		assertEquals(2, slotsByDate.get("2026-06-01").size());
	}

	@SuppressWarnings("unchecked")
	@Test
	void getByTeacher_noUnavailability_returnsEmpty() {
		when(repository.findAll()).thenReturn(List.of());

		Map<String, Object> result = service.getByTeacher(1L);

		Map<String, List<String>> slotsByDate = (Map<String, List<String>>) result.get("slotsByDate");
		assertTrue(slotsByDate.isEmpty());
	}

	@Test
	void getByTeacher_withOtherTeacherUnavailability_returnsEmpty() {
		Unavailability ua = new Unavailability(1L, 2L, "2026-06-01", List.of("08:00"));
		when(repository.findAll()).thenReturn(List.of(ua));

		Map<String, Object> result = service.getByTeacher(1L);

		@SuppressWarnings("unchecked")
		Map<String, List<String>> slotsByDate = (Map<String, List<String>>) result.get("slotsByDate");
		assertTrue(slotsByDate.isEmpty());
	}

	@Test
	void saveForTeacher_withExistingForOtherTeacher_onlyDeletesOwn() {
		Unavailability existing = new Unavailability(1L, 2L, "2026-06-01", List.of("08:00"));
		when(repository.findAll()).thenReturn(List.of(existing));

		Map<String, List<String>> newSlots = Map.of("2026-06-02", List.of("10:00"));
		service.saveForTeacher(1L, newSlots);

		verify(repository).deleteAll(List.of());
		verify(repository, atLeastOnce()).save(any());
	}

	@Test
	void saveForTeacher_deletesExistingAndSaves() {
		Unavailability existing = new Unavailability(1L, 1L, "2026-06-01", List.of("08:00"));
		when(repository.findAll()).thenReturn(List.of(existing));

		Map<String, List<String>> newSlots = Map.of("2026-06-02", List.of("10:00", "11:00"));
		service.saveForTeacher(1L, newSlots);

		verify(repository).deleteAll(List.of(existing));
		verify(repository, atLeastOnce()).save(any());
	}
}
