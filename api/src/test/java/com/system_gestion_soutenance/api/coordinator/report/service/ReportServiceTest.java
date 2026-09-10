package com.system_gestion_soutenance.api.coordinator.report.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseType;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.DefenseStatus;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.report.dto.GradeHistoryResponse;
import com.system_gestion_soutenance.api.coordinator.report.dto.SessionReportResponse;
import com.system_gestion_soutenance.api.coordinator.report.dto.TeacherWorkloadResponse;
import com.system_gestion_soutenance.api.coordinator.report.entity.GeneratedDocument;
import com.system_gestion_soutenance.api.coordinator.report.repository.GeneratedDocumentRepository;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationStatus;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportServiceTest {

	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);
	private final DefenseRepository defenseRepository = mock(DefenseRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final EvaluationRepository evaluationRepository = mock(EvaluationRepository.class);
	private final TeacherRepository teacherRepository = mock(TeacherRepository.class);
	private final GeneratedDocumentRepository generatedDocumentRepository = mock(GeneratedDocumentRepository.class);
	private final SecurityService securityService = mock(SecurityService.class);

	private final ReportService service = new ReportService(defenseSessionRepository, defenseRepository,
			groupRepository, projectRepository, evaluationRepository, teacherRepository, generatedDocumentRepository,
			securityService);

	@Test
	void getSessionReport_withValidSession_returnsReport() {
		DefenseSession session = new DefenseSession();
		session.setName("Session Test");
		session.setDefenseType(DefenseType.PFE);
		session.setStartDate(LocalDate.of(2025, 6, 1));
		session.setEndDate(LocalDate.of(2025, 6, 30));
		when(defenseSessionRepository.findById(1L)).thenReturn(java.util.Optional.of(session));

		Group group = mock(Group.class);
		when(group.getProject()).thenReturn(mock(Project.class));
		when(group.getProject().getId()).thenReturn(10L);
		when(group.getStudents()).thenReturn(List.of());
		when(groupRepository.findByDefenseSessionId(1L)).thenReturn(List.of(group));

		Project project = mock(Project.class);
		when(project.getId()).thenReturn(10L);
		when(project.getTitle()).thenReturn("Projet Test");

		Defense defense = mock(Defense.class);
		when(defense.getProject()).thenReturn(project);
		when(defense.getStatus()).thenReturn(DefenseStatus.COMPLETED);
		when(defense.getMembers()).thenReturn(List.of());
		when(defense.getDate()).thenReturn(LocalDate.of(2025, 6, 15));
		when(defense.getTime()).thenReturn(java.time.LocalTime.of(9, 0));
		when(defense.getFinalScore()).thenReturn(14.0);

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));

		SessionReportResponse result = service.getSessionReport(1L);

		assertEquals("Session Test", result.sessionName());
		assertEquals(1, result.totalProjects());
		assertEquals(1, result.passedProjects());
		assertEquals(100.0, result.passRate(), 0.01);
	}

	@Test
	void getSessionReport_sessionNotFound_throwsException() {
		when(defenseSessionRepository.findById(99L)).thenReturn(java.util.Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.getSessionReport(99L));
	}

	@Test
	void getSessionReport_excludesCancelledDefenses() {
		DefenseSession session = new DefenseSession();
		session.setName("Session Test");
		session.setDefenseType(DefenseType.PFE);
		session.setStartDate(LocalDate.of(2025, 6, 1));
		session.setEndDate(LocalDate.of(2025, 6, 30));
		when(defenseSessionRepository.findById(1L)).thenReturn(java.util.Optional.of(session));

		Project project = mock(Project.class);
		when(project.getId()).thenReturn(10L);
		when(project.getTitle()).thenReturn("Projet Test");

		Group group = mock(Group.class);
		when(group.getProject()).thenReturn(project);
		when(group.getStudents()).thenReturn(List.of());
		when(groupRepository.findByDefenseSessionId(1L)).thenReturn(List.of(group));

		Defense cancelled = mock(Defense.class);
		when(cancelled.getProject()).thenReturn(project);
		when(cancelled.getStatus()).thenReturn(DefenseStatus.CANCELLED);
		when(cancelled.getMembers()).thenReturn(List.of());

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(cancelled));

		SessionReportResponse result = service.getSessionReport(1L);

		assertEquals(0, result.totalProjects());
	}

	@Test
	void getTeacherWorkload_returnsWorkloadForAllTeachers() {
		Teacher teacher = new Teacher();
		teacher.setId(1L);
		teacher.setFirstName("Jean");
		teacher.setLastName("Dupont");
		when(teacherRepository.findAll()).thenReturn(List.of(teacher));

		Project supervised = mock(Project.class);
		when(supervised.getSupervisor()).thenReturn(teacher);
		when(supervised.getTitle()).thenReturn("Projet Encadré");
		when(projectRepository.findAll()).thenReturn(List.of(supervised));

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		List<TeacherWorkloadResponse> result = service.getTeacherWorkload();

		assertEquals(1, result.size());
		assertEquals("Jean Dupont", result.get(0).teacherName());
		assertEquals(1, result.get(0).supervisionCount());
	}

	@Test
	void getGradeHistory_returnsHistoryForSession() {
		Group group = mock(Group.class);
		when(group.getProject()).thenReturn(mock(Project.class));
		when(group.getProject().getId()).thenReturn(10L);
		when(groupRepository.findByDefenseSessionId(1L)).thenReturn(List.of(group));

		Project project = mock(Project.class);
		when(project.getId()).thenReturn(10L);
		when(project.getTitle()).thenReturn("Projet Test");
		when(projectRepository.findAllById(List.of(10L))).thenReturn(List.of(project));

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		when(evaluationRepository.findAll()).thenReturn(List.of());

		List<GradeHistoryResponse> result = service.getGradeHistory(1L);

		assertEquals(1, result.size());
		assertEquals("Projet Test", result.get(0).projectTitle());
	}

	@Test
	void recordDocumentGeneration_savesDocument() {
		when(securityService.getCurrentUserId()).thenReturn(1L);

		service.recordDocumentGeneration("SESSION_REPORT", 1L, 2048L);

		verify(generatedDocumentRepository).save(any(GeneratedDocument.class));
	}
}
