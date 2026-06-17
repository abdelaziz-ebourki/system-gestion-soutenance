package com.system_gestion_soutenance.api.coordinator.grade.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.grade.dto.GradeWeightedAverageResponse;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationStatus;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CoordinatorGradeServiceTest {

	private final DefenseRepository defenseRepository = mock(DefenseRepository.class);
	private final EvaluationRepository evaluationRepository = mock(EvaluationRepository.class);
	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);

	private final CoordinatorGradeService service = new CoordinatorGradeService(defenseRepository, evaluationRepository,
			defenseSessionRepository, groupRepository);

	private Teacher mockTeacher(Long id) {
		Teacher t = mock(Teacher.class);
		when(t.getId()).thenReturn(id);
		when(t.getFirstName()).thenReturn("John");
		when(t.getLastName()).thenReturn("Doe");
		return t;
	}

	private Project mockProject(Long id) {
		Project p = mock(Project.class);
		when(p.getId()).thenReturn(id);
		when(p.getTitle()).thenReturn("Projet " + id);
		return p;
	}

	@Test
	void getGrades_noDefenses_returnsEmpty() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		assertTrue(service.getGrades().isEmpty());
	}

	@Test
	void getGrades_noEvaluations_returnsNoEvaluations() {
		Teacher teacher = mockTeacher(10L);
		Project project = mockProject(1L);

		Defense defense = mock(Defense.class);
		when(defense.getProject()).thenReturn(project);
		when(defense.getMembers()).thenReturn(List.of(new JuryMember(null, teacher, "président", null)));
		when(defense.getDate()).thenReturn(LocalDate.of(2025, 6, 15));

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));
		when(evaluationRepository.findByDefenseIn(any())).thenReturn(List.of());

		var result = service.getGrades();

		assertEquals(1, result.size());
		assertEquals("no_evaluations", result.get(0).status());
		assertNull(result.get(0).finalScore());
	}

	@Test
	void getGrades_allEvaluationsSubmitted_returnsCompletedWithScore() {
		Teacher teacher = mockTeacher(10L);
		Project project = mockProject(1L);

		Defense defense = mock(Defense.class);
		when(defense.getProject()).thenReturn(project);
		when(defense.getMembers()).thenReturn(List.of(new JuryMember(null, teacher, "président", null)));
		when(defense.getDate()).thenReturn(LocalDate.of(2025, 6, 15));

		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setEvaluationCoefficients(Map.of("président", 2));

		Evaluation eval = mock(Evaluation.class);
		when(eval.getTeacherId()).thenReturn(10L);
		when(eval.getScore()).thenReturn(15.0);
		when(eval.getStatus()).thenReturn(EvaluationStatus.SUBMITTED);
		when(eval.getDefenseSessionId()).thenReturn(1L);
		when(eval.getDefense()).thenReturn(defense);

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));
		when(evaluationRepository.findByDefenseIn(any())).thenReturn(List.of(eval));
		when(defenseSessionRepository.findAllById(any())).thenReturn(List.of(ds));

		var result = service.getGrades();

		assertEquals(1, result.size());
		assertEquals("completed", result.get(0).status());
		assertNotNull(result.get(0).finalScore());
	}

	@Test
	void getGrades_usesDefenseDate() {
		Teacher teacher = mockTeacher(10L);
		Project project = mockProject(1L);

		Defense defense = mock(Defense.class);
		when(defense.getProject()).thenReturn(project);
		when(defense.getMembers()).thenReturn(List.of(new JuryMember(null, teacher, "président", null)));
		when(defense.getDate()).thenReturn(LocalDate.of(2025, 6, 15));

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));
		when(evaluationRepository.findByDefenseIn(any())).thenReturn(List.of());

		var result = service.getGrades();

		assertEquals("2025-06-15", result.get(0).defenseDate());
	}

	@Test
	void computeStatus_partialEvaluations_returnsPending() {
		Teacher teacher1 = mockTeacher(10L);
		Teacher teacher2 = mockTeacher(20L);
		Project project = mockProject(1L);

		Defense defense = mock(Defense.class);
		when(defense.getProject()).thenReturn(project);
		when(defense.getMembers()).thenReturn(List.of(new JuryMember(null, teacher1, "président", null),
				new JuryMember(null, teacher2, "examinateur", null)));
		when(defense.getDate()).thenReturn(LocalDate.of(2025, 6, 15));

		Evaluation eval = mock(Evaluation.class);
		when(eval.getTeacherId()).thenReturn(10L);
		when(eval.getScore()).thenReturn(15.0);
		when(eval.getStatus()).thenReturn(EvaluationStatus.SUBMITTED);
		when(eval.getDefense()).thenReturn(defense);

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));
		when(evaluationRepository.findByDefenseIn(any())).thenReturn(List.of(eval));

		var result = service.getGrades();

		assertEquals("awaiting", result.get(0).status());
	}

	@Test
	void computeWeightedScore_withNullCoefficient_usesZero() {
		Teacher teacher = mockTeacher(10L);
		Project project = mockProject(1L);

		Defense defense = mock(Defense.class);
		when(defense.getProject()).thenReturn(project);
		when(defense.getMembers()).thenReturn(List.of(new JuryMember(null, teacher, "unknown_role", null)));
		when(defense.getDate()).thenReturn(LocalDate.of(2025, 6, 15));

		Evaluation eval = mock(Evaluation.class);
		when(eval.getTeacherId()).thenReturn(10L);
		when(eval.getScore()).thenReturn(15.0);
		when(eval.getStatus()).thenReturn(EvaluationStatus.SUBMITTED);
		when(eval.getDefenseSessionId()).thenReturn(1L);
		when(eval.getDefense()).thenReturn(defense);

		DefenseSession ds = new DefenseSession();
		ds.setEvaluationCoefficients(Map.of());

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));
		when(evaluationRepository.findByDefenseIn(any())).thenReturn(List.of(eval));
		when(defenseSessionRepository.findAllById(any())).thenReturn(List.of(ds));

		var result = service.getGrades();

		assertNull(result.get(0).finalScore());
	}

	@Test
	void getGrades_resolvesSessionIdFromGroupWhenNoEvaluations() {
		Teacher teacher = mockTeacher(10L);
		Project project = mockProject(1L);

		Defense defense = mock(Defense.class);
		when(defense.getProject()).thenReturn(project);
		when(defense.getMembers()).thenReturn(List.of(new JuryMember(null, teacher, "président", null)));
		when(defense.getDate()).thenReturn(LocalDate.of(2025, 6, 15));

		DefenseSession ds = new DefenseSession();
		ds.setId(5L);
		Group group = mock(Group.class);
		when(group.getDefenseSession()).thenReturn(ds);

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));
		when(evaluationRepository.findByDefenseIn(any())).thenReturn(List.of());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(group));

		var result = service.getGrades();

		assertEquals(1, result.size());
	}
}
