package com.system_gestion_soutenance.api.coordinator.grade.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CoordinatorGradeServiceTest {

	private final JuryRepository juryRepository = mock(JuryRepository.class);
	private final EvaluationRepository evaluationRepository = mock(EvaluationRepository.class);
	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);
	private final SlotAssignmentRepository slotAssignmentRepository = mock(SlotAssignmentRepository.class);

	private final CoordinatorGradeService service = new CoordinatorGradeService(juryRepository, evaluationRepository,
			defenseSessionRepository, groupRepository, slotAssignmentRepository);

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
	void getGrades_noJuries_returnsEmpty() {
		when(juryRepository.findAll()).thenReturn(List.of());

		assertTrue(service.getGrades().isEmpty());
	}

	@Test
	void getGrades_noEvaluations_returnsNoEvaluations() {
		Teacher teacher = mockTeacher(10L);
		Project project = mockProject(1L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);
		when(member.getRoleName()).thenReturn("président");

		Jury jury = mock(Jury.class);
		when(jury.getProject()).thenReturn(project);
		when(jury.getMembers()).thenReturn(List.of(member));

		when(juryRepository.findAll()).thenReturn(List.of(jury));
		when(evaluationRepository.findByProjectId(1L)).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		var result = service.getGrades();

		assertEquals(1, result.size());
		assertEquals("no_evaluations", result.get(0).get("status"));
		assertNull(result.get(0).get("finalScore"));
	}

	@Test
	void getGrades_allEvaluationsSubmitted_returnsCompletedWithScore() {
		Teacher teacher = mockTeacher(10L);
		Project project = mockProject(1L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);
		when(member.getRoleName()).thenReturn("président");

		Jury jury = mock(Jury.class);
		when(jury.getProject()).thenReturn(project);
		when(jury.getMembers()).thenReturn(List.of(member));

		DefenseSession ds = new DefenseSession();
		ds.setEvaluationCoefficients(Map.of("président", 2));

		Evaluation eval = mock(Evaluation.class);
		when(eval.getTeacherId()).thenReturn(10L);
		when(eval.getScore()).thenReturn(15.0);
		when(eval.getStatus()).thenReturn("submitted");
		when(eval.getDefenseSessionId()).thenReturn(1L);

		when(juryRepository.findAll()).thenReturn(List.of(jury));
		when(evaluationRepository.findByProjectId(1L)).thenReturn(List.of(eval));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		var result = service.getGrades();

		assertEquals(1, result.size());
		assertEquals("completed", result.get(0).get("status"));
		assertNotNull(result.get(0).get("finalScore"));
	}

	@Test
	void getGrades_usesDefenseDateFromSlot() {
		Teacher teacher = mockTeacher(10L);
		Project project = mockProject(1L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);
		when(member.getRoleName()).thenReturn("président");

		Jury jury = mock(Jury.class);
		when(jury.getProject()).thenReturn(project);
		when(jury.getMembers()).thenReturn(List.of(member));

		SlotAssignment slot = mock(SlotAssignment.class);
		when(slot.getProjectId()).thenReturn(1L);
		when(slot.getDate()).thenReturn("2025-06-15");

		when(juryRepository.findAll()).thenReturn(List.of(jury));
		when(evaluationRepository.findByProjectId(1L)).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(slot));

		var result = service.getGrades();

		assertEquals("2025-06-15", result.get(0).get("defenseDate"));
	}

	@Test
	void computeStatus_partialEvaluations_returnsPending() {
		Teacher teacher = mockTeacher(10L);
		Teacher teacher2 = mockTeacher(20L);
		Project project = mockProject(1L);

		JuryMember member1 = mock(JuryMember.class);
		when(member1.getTeacher()).thenReturn(teacher);
		when(member1.getRoleName()).thenReturn("président");

		JuryMember member2 = mock(JuryMember.class);
		when(member2.getTeacher()).thenReturn(teacher2);
		when(member2.getRoleName()).thenReturn("examinateur");

		Jury jury = mock(Jury.class);
		when(jury.getProject()).thenReturn(project);
		when(jury.getMembers()).thenReturn(List.of(member1, member2));

		Evaluation eval = mock(Evaluation.class);
		when(eval.getTeacherId()).thenReturn(10L);
		when(eval.getScore()).thenReturn(15.0);
		when(eval.getStatus()).thenReturn("submitted");

		when(juryRepository.findAll()).thenReturn(List.of(jury));
		when(evaluationRepository.findByProjectId(1L)).thenReturn(List.of(eval));
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		var result = service.getGrades();

		assertEquals("pending", result.get(0).get("status"));
	}

	@Test
	void findDefenseDate_noMatchingSlot_returnsNull() {
		Teacher teacher = mockTeacher(10L);
		Project project = mockProject(1L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);
		when(member.getRoleName()).thenReturn("président");

		Jury jury = mock(Jury.class);
		when(jury.getProject()).thenReturn(project);
		when(jury.getMembers()).thenReturn(List.of(member));

		SlotAssignment slot = mock(SlotAssignment.class);
		when(slot.getProjectId()).thenReturn(99L);

		when(juryRepository.findAll()).thenReturn(List.of(jury));
		when(evaluationRepository.findByProjectId(1L)).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(slot));

		var result = service.getGrades();

		assertNull(result.get(0).get("defenseDate"));
	}

	@Test
	void computeWeightedScore_withNullCoefficient_usesZero() {
		Teacher teacher = mockTeacher(10L);
		Project project = mockProject(1L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);
		when(member.getRoleName()).thenReturn("unknown_role");

		Jury jury = mock(Jury.class);
		when(jury.getProject()).thenReturn(project);
		when(jury.getMembers()).thenReturn(List.of(member));

		Evaluation eval = mock(Evaluation.class);
		when(eval.getTeacherId()).thenReturn(10L);
		when(eval.getScore()).thenReturn(15.0);
		when(eval.getStatus()).thenReturn("submitted");
		when(eval.getDefenseSessionId()).thenReturn(1L);

		DefenseSession ds = new DefenseSession();
		ds.setEvaluationCoefficients(Map.of());

		when(juryRepository.findAll()).thenReturn(List.of(jury));
		when(evaluationRepository.findByProjectId(1L)).thenReturn(List.of(eval));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		var result = service.getGrades();

		assertNull(result.get(0).get("finalScore"));
	}

	@Test
	void getGrades_resolvesSessionIdFromGroupWhenNoEvaluations() {
		Teacher teacher = mockTeacher(10L);
		Project project = mockProject(1L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);
		when(member.getRoleName()).thenReturn("président");

		Jury jury = mock(Jury.class);
		when(jury.getProject()).thenReturn(project);
		when(jury.getMembers()).thenReturn(List.of(member));

		Group group = mock(Group.class);
		when(group.getSessionId()).thenReturn(5L);

		when(juryRepository.findAll()).thenReturn(List.of(jury));
		when(evaluationRepository.findByProjectId(1L)).thenReturn(List.of());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(group));
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		var result = service.getGrades();

		assertEquals(1, result.size());
	}
}
