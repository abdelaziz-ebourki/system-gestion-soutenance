package com.system_gestion_soutenance.api.teacher.evaluation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationSubmitRequest;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
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

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(15.0, "Good");
		Map<String, Object> result = service.submit(1L, req);

		assertEquals("submitted", result.get("status"));
		assertEquals(15.0, result.get("score"));
		assertEquals("Good", result.get("comment"));
		assertNotNull(result.get("submittedAt"));
	}

	@Test
	void submit_withNullScore_doesNotSetScore() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, 10L, "president", null, null, "pending", null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(projectRepository.findById(10L)).thenReturn(Optional.empty());

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(null, "Good");
		Map<String, Object> result = service.submit(1L, req);

		assertNull(result.get("score"));
		assertEquals("Good", result.get("comment"));
	}

	@Test
	void submit_withNullComment_doesNotSetComment() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, 10L, "president", null, null, "pending", null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(projectRepository.findById(10L)).thenReturn(Optional.empty());

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(15.0, null);
		Map<String, Object> result = service.submit(1L, req);

		assertEquals(15.0, result.get("score"));
		assertNull(result.get("comment"));
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

		List<Map<String, Object>> result = service.findByTeacher(1L);

		assertEquals("Bob Test", ((List<?>) result.get(0).get("studentNames")).get(0));
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

		List<Map<String, Object>> result = service.findByTeacher(1L);

		assertEquals("Alice Test", ((List<?>) result.get(0).get("studentNames")).get(0));
	}
}
