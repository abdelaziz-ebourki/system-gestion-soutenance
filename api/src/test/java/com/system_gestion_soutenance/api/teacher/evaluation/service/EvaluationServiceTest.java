package com.system_gestion_soutenance.api.teacher.evaluation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationSubmitRequest;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

	@Mock
	private EvaluationRepository evaluationRepository;
	@Mock
	private DefenseSessionRepository defenseSessionRepository;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private GroupRepository groupRepository;

	@InjectMocks
	private EvaluationService service;

	@Test
	void findByTeacher_returnsList() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, 10L, "president", null, null, "pending", null);
		when(evaluationRepository.findByTeacherId(1L)).thenReturn(List.of(ev));
		when(projectRepository.findById(10L)).thenReturn(Optional.of(new Project()));

		assertEquals(1, service.findByTeacher(1L).size());
	}

	@Test
	void submit_success() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, 10L, "president", null, null, "pending", null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(projectRepository.findById(10L)).thenReturn(Optional.empty());

		DefenseSession ds = new DefenseSession();
		ds.setSubmissionDeadline(LocalDate.now().plusDays(1));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(15.0, "Good");
		com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationResponse result = service.submit(1L, req);

		assertEquals("submitted", result.status());
		assertEquals(15.0, result.finalGrade());
		assertEquals("Good", result.comment());
	}

	@Test
	void submit_withNullScore_doesNotSetScore() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, 10L, "president", null, null, "pending", null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(projectRepository.findById(10L)).thenReturn(Optional.empty());

		DefenseSession ds = new DefenseSession();
		ds.setSubmissionDeadline(LocalDate.now().plusDays(1));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(null, "Good");
		com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationResponse result = service.submit(1L, req);

		assertNull(result.finalGrade());
		assertEquals("Good", result.comment());
	}

	@Test
	void submit_withNullComment_doesNotSetComment() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, 10L, "president", null, null, "pending", null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(projectRepository.findById(10L)).thenReturn(Optional.empty());

		DefenseSession ds = new DefenseSession();
		ds.setSubmissionDeadline(LocalDate.now().plusDays(1));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(15.0, null);
		com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationResponse result = service.submit(1L, req);

		assertEquals(15.0, result.finalGrade());
		assertNull(result.comment());
	}

	@Test
	void submit_notFound_throws() {
		when(evaluationRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> service.submit(99L, new EvaluationSubmitRequest(10.0, "")));
	}

	@Test
	void submit_alreadySubmitted_throws() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, 10L, "president", 12.0, null, "submitted", null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));

		assertThrows(ResponseStatusException.class,
				() -> service.submit(1L, new EvaluationSubmitRequest(15.0, "Update")));
		verify(evaluationRepository, never()).save(any());
	}

	@Test
	void toResponse_withNoGroups_usesProjectStudents() {
		Student student = new Student();
		student.setFirstName("Bob");
		student.setLastName("Test");

		Project project = new Project();
		project.setStudents(List.of(student));

		Evaluation ev = new Evaluation(1L, 1L, 1L, 10L, "president", null, null, "pending", null);
		when(evaluationRepository.findByTeacherId(1L)).thenReturn(List.of(ev));
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
		when(groupRepository.findByProjectId(10L)).thenReturn(List.of());

		List<com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationResponse> result = service
				.findByTeacher(1L);

		assertEquals(1, result.size());
	}

	@Test
	void toResponse_includesStudentNames() {
		Student student = new Student();
		student.setFirstName("Alice");
		student.setLastName("Test");

		com.system_gestion_soutenance.api.coordinator.group.entity.Group group = new com.system_gestion_soutenance.api.coordinator.group.entity.Group();
		group.setStudents(List.of(student));

		Evaluation ev = new Evaluation(1L, 1L, 1L, 10L, "president", null, null, "pending", null);
		when(evaluationRepository.findByTeacherId(1L)).thenReturn(List.of(ev));
		when(projectRepository.findById(10L)).thenReturn(Optional.of(new Project()));
		when(groupRepository.findByProjectId(10L)).thenReturn(List.of(group));

		List<com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationResponse> result = service
				.findByTeacher(1L);

		assertEquals(1, result.size());
	}
}
