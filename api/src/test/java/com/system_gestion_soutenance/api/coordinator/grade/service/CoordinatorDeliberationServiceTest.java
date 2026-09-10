package com.system_gestion_soutenance.api.coordinator.grade.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.grade.dto.DeliberationRequest;
import com.system_gestion_soutenance.api.coordinator.grade.dto.DeliberationStateResponse;
import com.system_gestion_soutenance.api.coordinator.grade.dto.ScoreAdjustRequest;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CoordinatorDeliberationServiceTest {

	@Mock
	private DefenseSessionRepository defenseSessionRepository;

	@Mock
	private DefenseRepository defenseRepository;

	@Mock
	private SecurityService securityService;

	@InjectMocks
	private CoordinatorDeliberationService service;

	private DefenseSession session() {
		DefenseSession session = new DefenseSession();
		session.setId(1L);
		session.setName("Session 2026");
		return session;
	}

	private Defense defenseWithProject(Long projectId, String title) {
		Defense defense = new Defense();
		Project project = new Project();
		project.setId(projectId);
		project.setTitle(title);
		defense.setProject(project);
		return defense;
	}

	@Test
	void getDeliberationState_unknownSession_throws404() {
		when(defenseSessionRepository.findById(9L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.getDeliberationState(9L));
	}

	@Test
	void getDeliberationState_skipsDefensesWithoutProject() {
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(session()));
		Defense orphan = new Defense();
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(orphan, defenseWithProject(3L, "IA")));

		DeliberationStateResponse state = service.getDeliberationState(1L);

		assertEquals(1, state.defenses().size());
		assertEquals(3L, state.defenses().get(0).projectId());
	}

	@Test
	void deliberate_unknownSession_throws404() {
		when(defenseSessionRepository.findById(9L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class,
				() -> service.deliberate(9L, new DeliberationRequest(Map.of(), null)));
	}

	@Test
	void deliberate_alreadyFinalized_throws400() {
		DefenseSession session = session();
		session.setDeliberatedAt(java.time.LocalDateTime.now());
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThrows(InvalidBusinessStateException.class,
				() -> service.deliberate(1L, new DeliberationRequest(Map.of(), null)));
	}

	@Test
	void deliberate_assignsScoresMentionsAndFinalizes() {
		DefenseSession session = session();
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(securityService.getCurrentUserId()).thenReturn(7L);
		Defense excellent = defenseWithProject(1L, "Cloud");
		Defense failing = defenseWithProject(2L, "Legacy");
		Defense unscored = defenseWithProject(3L, "IOT");
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(excellent, failing, unscored));

		DeliberationStateResponse state = service.deliberate(1L,
				new DeliberationRequest(Map.of(1L, 17.5, 2L, 8.0), "Bien dans l'ensemble"));

		assertEquals("TRES_BIEN", excellent.getMention());
		assertEquals(17.5, excellent.getFinalScore());
		assertEquals("Bien dans l'ensemble", excellent.getDeliberationComment());
		assertEquals("INSUFFISANT", failing.getMention());
		assertTrue(unscored.getFinalScore() == null);
		assertEquals(7L, session.getDeliberatedBy());
		assertTrue(session.getDeliberatedAt() != null);
		verify(defenseRepository).saveAll(any());
		verify(defenseSessionRepository).save(session);
		assertEquals(3, state.defenses().size());
	}

	@Test
	void mentionBands_coverAllThresholds() {
		long[] ids = {11L, 12L, 13L, 14L};
		double[] scores = {16.0, 14.0, 12.0, 10.0};
		String[] expected = {"TRES_BIEN", "BIEN", "ASSEZ_BIEN", "PASSABLE"};
		DefenseSession session = session();
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(securityService.getCurrentUserId()).thenReturn(7L);
		java.util.List<Defense> defenses = new java.util.ArrayList<>();
		java.util.Map<Long, Double> finalScores = new java.util.HashMap<>();
		for (int i = 0; i < ids.length; i++) {
			defenses.add(defenseWithProject(ids[i], "P" + i));
			finalScores.put(ids[i], scores[i]);
		}
		when(defenseRepository.findAllWithMembers()).thenReturn(defenses);

		service.deliberate(1L, new DeliberationRequest(finalScores, null));

		for (int i = 0; i < ids.length; i++) {
			assertEquals(expected[i], defenses.get(i).getMention());
		}
	}

	@Test
	void adjustScore_unknownDefense_throws404() {
		when(defenseRepository.findById(9L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.adjustScore(9L, new ScoreAdjustRequest(12.0, null)));
	}

	@Test
	void adjustScore_updatesScoreMentionAndOptionalComment() {
		Defense defense = defenseWithProject(1L, "Cloud");
		when(defenseRepository.findById(4L)).thenReturn(Optional.of(defense));

		service.adjustScore(4L, new ScoreAdjustRequest(15.0, "Bonus jury"));

		assertEquals(15.0, defense.getFinalScore());
		assertEquals("BIEN", defense.getMention());
		assertEquals("Bonus jury", defense.getDeliberationComment());
		verify(defenseRepository).save(defense);
	}

	@Test
	void adjustScore_nullComment_keepsExistingComment() {
		Defense defense = defenseWithProject(1L, "Cloud");
		defense.setDeliberationComment("Initial");
		when(defenseRepository.findById(4L)).thenReturn(Optional.of(defense));

		service.adjustScore(4L, new ScoreAdjustRequest(11.0, null));

		assertEquals("PASSABLE", defense.getMention());
		assertEquals("Initial", defense.getDeliberationComment());
	}
}
