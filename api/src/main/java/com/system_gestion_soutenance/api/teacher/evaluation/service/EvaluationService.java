package com.system_gestion_soutenance.api.teacher.evaluation.service;

import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationSubmitRequest;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationStatus;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.notification.event.EvaluationSubmittedEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
public class EvaluationService {

	private final EvaluationRepository evaluationRepository;
	private final DefenseSessionRepository defenseSessionRepository;
	private final ProjectRepository projectRepository;
	private final GroupRepository groupRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final SecurityService securityService;

	public EvaluationService(EvaluationRepository evaluationRepository,
			DefenseSessionRepository defenseSessionRepository, ProjectRepository projectRepository,
			GroupRepository groupRepository, ApplicationEventPublisher eventPublisher,
			SecurityService securityService) {
		this.evaluationRepository = evaluationRepository;
		this.defenseSessionRepository = defenseSessionRepository;
		this.projectRepository = projectRepository;
		this.groupRepository = groupRepository;
		this.eventPublisher = eventPublisher;
		this.securityService = securityService;
	}

	public List<Evaluation> findByTeacher(Long teacherId) {
		return evaluationRepository.findByTeacherId(teacherId);
	}

	public PaginatedResponse<Evaluation> findByTeacher(Long teacherId, int page, int limit) {
		Page<Evaluation> evalPage = evaluationRepository.findByTeacherId(teacherId, PageRequest.of(page, limit));
		return new PaginatedResponse<>(evalPage.getContent(), evalPage.getTotalElements(), evalPage.getTotalPages(),
				page, limit);
	}

	public Map<Long, Project> buildProjectMap(List<Evaluation> evaluations) {
		List<Long> projectIds = evaluations.stream()
				.map(e -> e.getDefense() != null && e.getDefense().getProject() != null
						? e.getDefense().getProject().getId()
						: null)
				.filter(Objects::nonNull).distinct().toList();
		return projectRepository.findAllById(projectIds).stream().collect(Collectors.toMap(Project::getId, p -> p));
	}

	@Audited(action = "UPDATE", entity = "Evaluation")
	@Transactional
	public Evaluation submit(Long id, Long currentUserId, EvaluationSubmitRequest request) {
		Evaluation evaluation = evaluationRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Évaluation non trouvée"));

		if (!evaluation.getTeacherId().equals(currentUserId)) {
			throw new UnauthorizedAccessException("Vous ne pouvez soumettre que vos propres évaluations");
		}

		if (evaluation.getStatus() == EvaluationStatus.SUBMITTED) {
			throw new InvalidBusinessStateException("Cette évaluation a déjà été soumise");
		}

		DefenseSession ds = defenseSessionRepository.findById(evaluation.getDefenseSessionId())
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));

		if (ds.isFrozen()) {
			throw new InvalidBusinessStateException("Cette session est gelée. Les soumissions de notes sont bloquées.");
		}

		if (ds.getSubmissionDeadline() != null && LocalDate.now().isAfter(ds.getSubmissionDeadline())) {
			throw new InvalidBusinessStateException("La date limite de soumission des évaluations est dépassée");
		}

		if (request.score() != null)
			evaluation.setScore(request.score());
		if (request.comment() != null)
			evaluation.setComment(request.comment());
		if (request.attendanceStatus() != null)
			evaluation.setAttendanceStatus(request.attendanceStatus());

		evaluation.setStatus(EvaluationStatus.SUBMITTED);
		evaluation.setSubmittedAt(LocalDateTime.now());
		Evaluation saved = evaluationRepository.save(evaluation);

		String projectTitle = "Inconnu";
		if (saved.getDefense() != null && saved.getDefense().getProject() != null) {
			projectTitle = saved.getDefense().getProject().getTitle();
		}

		eventPublisher.publishEvent(new EvaluationSubmittedEvent(securityService.getCurrentUserEmail(), saved.getId(),
				projectTitle, saved.getScore() != null ? saved.getScore().doubleValue() : 0.0));
		return saved;
	}
}