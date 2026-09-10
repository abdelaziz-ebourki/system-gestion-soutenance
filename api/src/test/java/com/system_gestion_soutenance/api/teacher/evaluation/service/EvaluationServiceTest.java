package com.system_gestion_soutenance.api.teacher.evaluation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationSubmitRequest;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationStatus;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationType;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedAccessException;
import org.springframework.context.ApplicationEventPublisher;
import com.system_gestion_soutenance.api.common.service.SecurityService;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

	@Mock
	private EvaluationRepository evaluationRepository;
	@Mock
	private DefenseSessionRepository defenseSessionRepository;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private DefenseRepository defenseRepository;
	@Mock
	private GroupRepository groupRepository;
	@Mock
	private ApplicationEventPublisher eventPublisher;
	@Mock
	private SecurityService securityService;

	@InjectMocks
	private EvaluationService service;

	private static Defense mockDefense() {
		return mock(Defense.class);
	}

	@Test
	void findByTeacher_returnsList() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", EvaluationType.SOUTENANCE, null, null,
				EvaluationStatus.PENDING, null, null);
		when(evaluationRepository.findByTeacherId(1L)).thenReturn(List.of(ev));

		assertEquals(1, service.findByTeacher(1L).size());
	}

	@Test
	void submit_success() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", EvaluationType.SOUTENANCE, null, null,
				EvaluationStatus.PENDING, null, null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		DefenseSession ds = new DefenseSession();
		ds.setSubmissionDeadline(LocalDate.now().plusDays(1));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(securityService.getCurrentUserEmail()).thenReturn("teacher@test.com");

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(15.0, "Good", null);
		Evaluation result = service.submit(1L, 1L, req);

		assertEquals(EvaluationStatus.SUBMITTED, result.getStatus());
		assertEquals(15.0, result.getScore());
		assertEquals("Good", result.getComment());
	}

	@Test
	void submit_withNullScore_doesNotSetScore() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", EvaluationType.SOUTENANCE, null, null,
				EvaluationStatus.PENDING, null, null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		DefenseSession ds = new DefenseSession();
		ds.setSubmissionDeadline(LocalDate.now().plusDays(1));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(securityService.getCurrentUserEmail()).thenReturn("teacher@test.com");

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(null, "Good", null);
		Evaluation result = service.submit(1L, 1L, req);

		assertNull(result.getScore());
		assertEquals("Good", result.getComment());
	}

	@Test
	void submit_withNullComment_doesNotSetComment() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", EvaluationType.SOUTENANCE, null, null,
				EvaluationStatus.PENDING, null, null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		DefenseSession ds = new DefenseSession();
		ds.setSubmissionDeadline(LocalDate.now().plusDays(1));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(securityService.getCurrentUserEmail()).thenReturn("teacher@test.com");

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(15.0, null, null);
		Evaluation result = service.submit(1L, 1L, req);

		assertEquals(15.0, result.getScore());
		assertNull(result.getComment());
	}

	@Test
	void submit_notFound_throws() {
		when(evaluationRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class,
				() -> service.submit(99L, 1L, new EvaluationSubmitRequest(10.0, "", null)));
	}

	@Test
	void submit_alreadySubmitted_throws() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", EvaluationType.SOUTENANCE, 12.0, null,
				EvaluationStatus.SUBMITTED, null, null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));

		assertThrows(InvalidBusinessStateException.class,
				() -> service.submit(1L, 1L, new EvaluationSubmitRequest(15.0, "Update", null)));
		verify(evaluationRepository, never()).save(any());
	}

	@Test
	void findByTeacher_returnsListWithProject() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", EvaluationType.SOUTENANCE, null, null,
				EvaluationStatus.PENDING, null, null);
		when(evaluationRepository.findByTeacherId(1L)).thenReturn(List.of(ev));

		List<Evaluation> result = service.findByTeacher(1L);

		assertEquals(1, result.size());
	}

	@Test
	void submit_wrongTeacher_throws() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", EvaluationType.SOUTENANCE, null, null,
				EvaluationStatus.PENDING, null, null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));

		assertThrows(UnauthorizedAccessException.class,
				() -> service.submit(1L, 99L, new EvaluationSubmitRequest(15.0, "Update", null)));
		verify(evaluationRepository, never()).save(any());
	}

	@Test
	void submit_frozenSession_throws() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", EvaluationType.SOUTENANCE, null, null,
				EvaluationStatus.PENDING, null, null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));

		DefenseSession ds = new DefenseSession();
		ds.setSubmissionDeadline(LocalDate.now().plusDays(1));
		ds.setFrozen(true);
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		assertThrows(InvalidBusinessStateException.class,
				() -> service.submit(1L, 1L, new EvaluationSubmitRequest(15.0, "Score", null)));
		verify(evaluationRepository, never()).save(any());
	}

	@Test
	void submit_withAttendanceStatus_persistsAttendance() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", EvaluationType.SOUTENANCE, null, null,
				EvaluationStatus.PENDING, null, null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(securityService.getCurrentUserEmail()).thenReturn("teacher@test.com");

		DefenseSession ds = new DefenseSession();
		ds.setSubmissionDeadline(LocalDate.now().plusDays(1));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(15.0, "Good",
				com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationAttendanceStatus.PRESENT);
		Evaluation result = service.submit(1L, 1L, req);

		assertEquals(com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationAttendanceStatus.PRESENT,
				result.getAttendanceStatus());
	}

	private Teacher teacher(Long id) {
		Teacher teacher = new Teacher();
		teacher.setId(id);
		return teacher;
	}

	private Project project(Long id, Long supervisorId) {
		Project project = new Project();
		project.setId(id);
		project.setTitle("Projet " + id);
		if (supervisorId != null) {
			project.setSupervisor(teacher(supervisorId));
		}
		return project;
	}

	private Defense defenseWithJuryMember(Long defenseId, Project project, Long teacherId) {
		Defense defense = new Defense();
		defense.setId(defenseId);
		defense.setProject(project);
		JuryMember member = new JuryMember();
		member.setTeacher(teacher(teacherId));
		member.setRoleName("Rapporteur");
		defense.setMembers(java.util.List.of(member));
		return defense;
	}

	private Group groupInSession(Long sessionId) {
		Group group = new Group();
		if (sessionId != null) {
			DefenseSession session = new DefenseSession();
			session.setId(sessionId);
			group.setDefenseSession(session);
		}
		return group;
	}

	private DefenseSession openSession() {
		DefenseSession session = new DefenseSession();
		session.setId(1L);
		session.setSubmissionDeadline(LocalDate.now().plusDays(1));
		return session;
	}

	private void mockOpenSessionFlow(Project project) {
		Group group = groupInSession(1L);
		when(groupRepository.findByProjectId(project.getId())).thenReturn(java.util.List.of(group));
		when(defenseSessionRepository.findById(1L)).thenReturn(java.util.Optional.of(openSession()));
	}

	@Test
	void buildProjectMap_skipsEvaluationsWithoutProject() {
		Evaluation withProject = new Evaluation(1L, 1L, 1L, null, "president", EvaluationType.SOUTENANCE, null, null,
				EvaluationStatus.PENDING, null, null);
		Defense defense = new Defense();
		defense.setProject(project(3L, null));
		withProject.setDefense(defense);
		Evaluation withoutDefense = new Evaluation(2L, 1L, 1L, null, "president", EvaluationType.SOUTENANCE, null, null,
				EvaluationStatus.PENDING, null, null);
		when(projectRepository.findAllById(java.util.List.of(3L))).thenReturn(java.util.List.of(project(3L, null)));

		java.util.Map<Long, Project> map = service.buildProjectMap(java.util.List.of(withProject, withoutDefense));

		assertEquals(1, map.size());
		assertEquals("Projet 3", map.get(3L).getTitle());
	}

	@Test
	void submitDefense_unknownDefense_throws() {
		when(defenseRepository.findById(9L)).thenReturn(java.util.Optional.empty());

		assertThrows(EntityNotFoundException.class,
				() -> service.submitDefense(9L, 1L, new EvaluationSubmitRequest(12.0, null, null)));
	}

	@Test
	void submitDefense_nonMember_throws() {
		Defense defense = defenseWithJuryMember(1L, project(3L, 5L), 2L);
		when(defenseRepository.findById(1L)).thenReturn(java.util.Optional.of(defense));

		assertThrows(UnauthorizedAccessException.class,
				() -> service.submitDefense(1L, 9L, new EvaluationSubmitRequest(12.0, null, null)));
	}

	@Test
	void submitDefense_noSessionForProject_throws() {
		Defense defense = defenseWithJuryMember(1L, project(3L, 5L), 2L);
		when(defenseRepository.findById(1L)).thenReturn(java.util.Optional.of(defense));
		when(groupRepository.findByProjectId(3L)).thenReturn(java.util.List.of(groupInSession(null)));

		assertThrows(InvalidBusinessStateException.class,
				() -> service.submitDefense(1L, 2L, new EvaluationSubmitRequest(12.0, null, null)));
	}

	@Test
	void submitDefense_frozenSession_throws() {
		Defense defense = defenseWithJuryMember(1L, project(3L, 5L), 2L);
		when(defenseRepository.findById(1L)).thenReturn(java.util.Optional.of(defense));
		Group group = groupInSession(1L);
		when(groupRepository.findByProjectId(3L)).thenReturn(java.util.List.of(group));
		DefenseSession frozen = openSession();
		frozen.setFrozen(true);
		when(defenseSessionRepository.findById(1L)).thenReturn(java.util.Optional.of(frozen));

		assertThrows(InvalidBusinessStateException.class,
				() -> service.submitDefense(1L, 2L, new EvaluationSubmitRequest(12.0, null, null)));
	}

	@Test
	void submitDefense_createsEvaluationWhenMissing() {
		Project proj = project(3L, 5L);
		Defense defense = defenseWithJuryMember(1L, proj, 2L);
		when(defenseRepository.findById(1L)).thenReturn(java.util.Optional.of(defense));
		mockOpenSessionFlow(proj);
		when(evaluationRepository.findByDefenseAndTeacherIdAndType(defense, 2L, EvaluationType.SOUTENANCE))
				.thenReturn(java.util.Optional.empty());
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(securityService.getCurrentUserEmail()).thenReturn("jury@test.com");

		Evaluation result = service.submitDefense(1L, 2L, new EvaluationSubmitRequest(14.5, "Solide", null));

		assertEquals(EvaluationStatus.SUBMITTED, result.getStatus());
		assertEquals(14.5, result.getScore());
		assertEquals("Solide", result.getComment());
		assertEquals("Rapporteur", result.getRole());
	}

	@Test
	void submitDefense_updatesExistingEvaluationWithNulls() {
		Project proj = project(3L, 5L);
		Defense defense = defenseWithJuryMember(1L, proj, 2L);
		when(defenseRepository.findById(1L)).thenReturn(java.util.Optional.of(defense));
		mockOpenSessionFlow(proj);
		Evaluation existing = new Evaluation(9L, 2L, 1L, defense, "Rapporteur", EvaluationType.SOUTENANCE, 10.0,
				"Ancien", EvaluationStatus.PENDING, null, null);
		when(evaluationRepository.findByDefenseAndTeacherIdAndType(defense, 2L, EvaluationType.SOUTENANCE))
				.thenReturn(java.util.Optional.of(existing));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(securityService.getCurrentUserEmail()).thenReturn("jury@test.com");

		Evaluation result = service.submitDefense(1L, 2L, new EvaluationSubmitRequest(null, null, null));

		assertEquals(10.0, result.getScore());
		assertEquals("Ancien", result.getComment());
		assertEquals(EvaluationStatus.SUBMITTED, result.getStatus());
	}

	@Test
	void submitRapport_unknownDefense_throws() {
		when(defenseRepository.findById(9L)).thenReturn(java.util.Optional.empty());

		assertThrows(EntityNotFoundException.class,
				() -> service.submitRapport(9L, 5L, new EvaluationSubmitRequest(12.0, null, null)));
	}

	@Test
	void submitRapport_defenseWithoutProject_throws() {
		Defense defense = new Defense();
		defense.setId(1L);
		when(defenseRepository.findById(1L)).thenReturn(java.util.Optional.of(defense));

		assertThrows(InvalidBusinessStateException.class,
				() -> service.submitRapport(1L, 5L, new EvaluationSubmitRequest(12.0, null, null)));
	}

	@Test
	void submitRapport_nonSupervisor_throws() {
		Defense defense = new Defense();
		defense.setId(1L);
		defense.setProject(project(3L, 5L));
		when(defenseRepository.findById(1L)).thenReturn(java.util.Optional.of(defense));

		assertThrows(UnauthorizedAccessException.class,
				() -> service.submitRapport(1L, 9L, new EvaluationSubmitRequest(12.0, null, null)));
	}

	@Test
	void submitRapport_updatesExistingEvaluation() {
		Project proj = project(3L, 5L);
		Defense defense = new Defense();
		defense.setId(1L);
		defense.setProject(proj);
		when(defenseRepository.findById(1L)).thenReturn(java.util.Optional.of(defense));
		mockOpenSessionFlow(proj);
		Evaluation existing = new Evaluation(9L, 5L, 1L, defense, "Rapporteur", EvaluationType.RAPPORT, 9.0, null,
				EvaluationStatus.PENDING, null, null);
		when(evaluationRepository.findByDefenseAndType(defense, EvaluationType.RAPPORT))
				.thenReturn(java.util.List.of(existing));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(securityService.getCurrentUserEmail()).thenReturn("sup@test.com");

		Evaluation result = service.submitRapport(1L, 5L, new EvaluationSubmitRequest(13.0, "Mieux", null));

		assertEquals(13.0, result.getScore());
		assertEquals(EvaluationStatus.SUBMITTED, result.getStatus());
	}

	@Test
	void submitRapport_createsEvaluationWhenMissing() {
		Project proj = project(3L, 5L);
		Defense defense = new Defense();
		defense.setId(1L);
		defense.setProject(proj);
		when(defenseRepository.findById(1L)).thenReturn(java.util.Optional.of(defense));
		mockOpenSessionFlow(proj);
		when(evaluationRepository.findByDefenseAndType(defense, EvaluationType.RAPPORT))
				.thenReturn(java.util.List.of());
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(securityService.getCurrentUserEmail()).thenReturn("sup@test.com");

		Evaluation result = service.submitRapport(1L, 5L, new EvaluationSubmitRequest(16.0, null, null));

		assertEquals("Rapporteur", result.getRole());
		assertEquals(16.0, result.getScore());
	}
}
