package com.system_gestion_soutenance.api.teacher.stats.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryMemberRepository;
import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherStatsServiceTest {

	@Mock
	private EvaluationRepository evaluationRepository;
	@Mock
	private JuryMemberRepository juryMemberRepository;
	@Mock
	private UnavailabilityRepository unavailabilityRepository;

	@InjectMocks
	private TeacherStatsService service;

	@Test
	void getStats_withNullSlots_countsZero() {
		Evaluation pending = new Evaluation();
		pending.setStatus("pending");

		Unavailability ua = new Unavailability();
		ua.setTeacherId(1L);
		ua.setSlots(null);

		when(evaluationRepository.findByTeacherId(1L)).thenReturn(List.of(pending));
		when(unavailabilityRepository.findAll()).thenReturn(List.of(ua));
		when(juryMemberRepository.findByTeacher_Id(1L)).thenReturn(List.of());

		com.system_gestion_soutenance.api.teacher.stats.dto.TeacherStatsResponse result = service.getStats(1L);

		assertEquals(0L, result.declaredUnavailabilitySlots());
	}

	@Test
	void getStats_returnsStats() {
		Evaluation pending = new Evaluation();
		pending.setStatus("pending");
		Evaluation submitted = new Evaluation();
		submitted.setStatus("submitted");

		Unavailability ua = new Unavailability();
		ua.setTeacherId(1L);
		ua.setSlots(List.of("08:00", "09:00"));

		when(evaluationRepository.findByTeacherId(1L)).thenReturn(List.of(pending, submitted));
		when(unavailabilityRepository.findAll()).thenReturn(List.of(ua));
		when(juryMemberRepository.findByTeacher_Id(1L)).thenReturn(List.of());

		com.system_gestion_soutenance.api.teacher.stats.dto.TeacherStatsResponse result = service.getStats(1L);

		assertEquals(0, result.upcomingDefenses());
		assertEquals(1L, result.pendingEvaluations());
		assertEquals(2L, result.declaredUnavailabilitySlots());
		assertEquals(0L, result.juryAssignments());
	}
}
