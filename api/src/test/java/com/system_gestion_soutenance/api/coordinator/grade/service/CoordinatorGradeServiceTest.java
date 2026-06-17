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
import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationType;
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
		when(defense.getMembers())
				.thenReturn(List.of(new JuryMember(null, teacher, "président", null, null, null, null)));
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
		when(defense.getMembers())
				.thenReturn(List.of(new JuryMember(null, teacher, "président", null, null, null, null)));
		when(defense.getDate()).thenReturn(LocalDate.of(2025, 6, 15));

		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setEvaluationCoefficients(Map.of("président", 2));
		ds.setRapportCoefficient(0);
		ds.setSoutenanceCoefficient(100);

		Evaluation eval = mock(Evaluation.class);
		when(eval.getTeacherId()).thenReturn(10L);
		when(eval.getScore()).thenReturn(15.0);
		when(eval.getStatus()).thenReturn(EvaluationStatus.SUBMITTED);
		when(eval.getDefenseSessionId()).thenReturn(1L);
		when(eval.getDefense()).thenReturn(defense);
		when(eval.getType()).thenReturn(EvaluationType.SOUTENANCE);

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
		when(defense.getMembers())
				.thenReturn(List.of(new JuryMember(null, teacher, "président", null, null, null, null)));
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
		when(defense.getMembers())
				.thenReturn(List.of(new JuryMember(null, teacher1, "président", null, null, null, null),
						new JuryMember(null, teacher2, "examinateur", null, null, null, null)));
		when(defense.getDate()).thenReturn(LocalDate.of(2025, 6, 15));

		Evaluation eval = mock(Evaluation.class);
		when(eval.getTeacherId()).thenReturn(10L);
		when(eval.getScore()).thenReturn(15.0);
		when(eval.getStatus()).thenReturn(EvaluationStatus.SUBMITTED);
		when(eval.getType()).thenReturn(EvaluationType.SOUTENANCE);
		when(eval.getDefense()).thenReturn(defense);
		when(eval.getDefenseSessionId()).thenReturn(1L);

		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setRapportCoefficient(0);
		ds.setSoutenanceCoefficient(100);
		ds.setEvaluationCoefficients(Map.of("président", 1, "examinateur", 1));

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));
		when(evaluationRepository.findByDefenseIn(any())).thenReturn(List.of(eval));
		when(defenseSessionRepository.findAllById(any())).thenReturn(List.of(ds));

		var result = service.getGrades();

		assertEquals("awaiting", result.get(0).status());
	}

	@Test
	void computeWeightedScore_withNullCoefficient_usesZero() {
		Teacher teacher = mockTeacher(10L);
		Project project = mockProject(1L);

		Defense defense = mock(Defense.class);
		when(defense.getProject()).thenReturn(project);
		when(defense.getMembers())
				.thenReturn(List.of(new JuryMember(null, teacher, "unknown_role", null, null, null, null)));
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
		when(defense.getMembers())
				.thenReturn(List.of(new JuryMember(null, teacher, "président", null, null, null, null)));
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

	@Test
	void computeWeightedScore_withRapportAndSoutenance_usesCorrectFormula() {
		Teacher teacher1 = mockTeacher(10L);
		Teacher teacher2 = mockTeacher(20L);
		Project project = mockProject(1L);

		Defense defense = mock(Defense.class);
		when(defense.getProject()).thenReturn(project);
		when(defense.getMembers())
				.thenReturn(List.of(new JuryMember(null, teacher1, "Président", null, null, null, null),
						new JuryMember(null, teacher2, "Examinateur", null, null, null, null)));
		when(defense.getDate()).thenReturn(LocalDate.of(2025, 6, 15));

		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setRapportCoefficient(30);
		ds.setSoutenanceCoefficient(70);
		ds.setEvaluationCoefficients(Map.of("Président", 2, "Examinateur", 1));

		Evaluation rapportEval = mock(Evaluation.class);
		when(rapportEval.getType()).thenReturn(EvaluationType.RAPPORT);
		when(rapportEval.getScore()).thenReturn(16.0);
		when(rapportEval.getStatus()).thenReturn(EvaluationStatus.SUBMITTED);
		when(rapportEval.getDefense()).thenReturn(defense);
		when(rapportEval.getDefenseSessionId()).thenReturn(1L);

		Evaluation soutenance1 = mock(Evaluation.class);
		when(soutenance1.getType()).thenReturn(EvaluationType.SOUTENANCE);
		when(soutenance1.getTeacherId()).thenReturn(10L);
		when(soutenance1.getScore()).thenReturn(14.0);
		when(soutenance1.getStatus()).thenReturn(EvaluationStatus.SUBMITTED);
		when(soutenance1.getDefense()).thenReturn(defense);
		when(soutenance1.getDefenseSessionId()).thenReturn(1L);

		Evaluation soutenance2 = mock(Evaluation.class);
		when(soutenance2.getType()).thenReturn(EvaluationType.SOUTENANCE);
		when(soutenance2.getTeacherId()).thenReturn(20L);
		when(soutenance2.getScore()).thenReturn(11.0);
		when(soutenance2.getStatus()).thenReturn(EvaluationStatus.SUBMITTED);
		when(soutenance2.getDefense()).thenReturn(defense);
		when(soutenance2.getDefenseSessionId()).thenReturn(1L);

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));
		when(evaluationRepository.findByDefenseIn(any())).thenReturn(List.of(rapportEval, soutenance1, soutenance2));
		when(defenseSessionRepository.findAllById(any())).thenReturn(List.of(ds));

		var result = service.getGrades();

		// avgSoutenance = (14*2 + 11*1) / 3 = 39 / 3 = 13.0
		// finalScore = (16 * 30 + 13 * 70) / 100 = (480 + 910) / 100 = 13.9
		assertEquals(13.9, result.get(0).finalScore());
	}
}
