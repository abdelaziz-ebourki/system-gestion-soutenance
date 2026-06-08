package com.system_gestion_soutenance.api.teacher.evaluation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationSubmitRequest;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationStatus;
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

	private static Defense mockDefense() {
		return mock(Defense.class);
	}

	@Test
	void findByTeacher_returnsList() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", null, null, EvaluationStatus.PENDING,
				null);
		when(evaluationRepository.findByTeacherId(1L)).thenReturn(List.of(ev));

		assertEquals(1, service.findByTeacher(1L).size());
	}

	@Test
	void submit_success() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", null, null, EvaluationStatus.PENDING,
				null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		DefenseSession ds = new DefenseSession();
		ds.setSubmissionDeadline(LocalDate.now().plusDays(1));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(15.0, "Good");
		Evaluation result = service.submit(1L, req);

		assertEquals(EvaluationStatus.SUBMITTED, result.getStatus());
		assertEquals(15.0, result.getScore());
		assertEquals("Good", result.getComment());
	}

	@Test
	void submit_withNullScore_doesNotSetScore() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", null, null, EvaluationStatus.PENDING,
				null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		DefenseSession ds = new DefenseSession();
		ds.setSubmissionDeadline(LocalDate.now().plusDays(1));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(null, "Good");
		Evaluation result = service.submit(1L, req);

		assertNull(result.getScore());
		assertEquals("Good", result.getComment());
	}

	@Test
	void submit_withNullComment_doesNotSetComment() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", null, null, EvaluationStatus.PENDING,
				null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
		when(evaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		DefenseSession ds = new DefenseSession();
		ds.setSubmissionDeadline(LocalDate.now().plusDays(1));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		EvaluationSubmitRequest req = new EvaluationSubmitRequest(15.0, null);
		Evaluation result = service.submit(1L, req);

		assertEquals(15.0, result.getScore());
		assertNull(result.getComment());
	}

	@Test
	void submit_notFound_throws() {
		when(evaluationRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class, () -> service.submit(99L, new EvaluationSubmitRequest(10.0, "")));
	}

	@Test
	void submit_alreadySubmitted_throws() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", 12.0, null, EvaluationStatus.SUBMITTED,
				null);
		when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));

		assertThrows(InvalidBusinessStateException.class,
				() -> service.submit(1L, new EvaluationSubmitRequest(15.0, "Update")));
		verify(evaluationRepository, never()).save(any());
	}

	@Test
	void findByTeacher_returnsListWithProject() {
		Evaluation ev = new Evaluation(1L, 1L, 1L, mockDefense(), "president", null, null, EvaluationStatus.PENDING,
				null);
		when(evaluationRepository.findByTeacherId(1L)).thenReturn(List.of(ev));

		List<Evaluation> result = service.findByTeacher(1L);

		assertEquals(1, result.size());
	}
}
