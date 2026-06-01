package com.system_gestion_soutenance.api.teacher.evaluation.service;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationSubmitRequest;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EvaluationService {

	private final EvaluationRepository evaluationRepository;
	private final DefenseSessionRepository defenseSessionRepository;
	private final ProjectRepository projectRepository;
	private final GroupRepository groupRepository;

	public EvaluationService(EvaluationRepository evaluationRepository,
			DefenseSessionRepository defenseSessionRepository, ProjectRepository projectRepository,
			GroupRepository groupRepository) {
		this.evaluationRepository = evaluationRepository;
		this.defenseSessionRepository = defenseSessionRepository;
		this.projectRepository = projectRepository;
		this.groupRepository = groupRepository;
	}

	public List<Map<String, Object>> findByTeacher(Long teacherId) {
		return evaluationRepository.findByTeacherId(teacherId).stream().map(this::toResponse)
				.collect(Collectors.toList());
	}

	public Map<String, Object> submit(Long id, EvaluationSubmitRequest request) {
		Evaluation evaluation = evaluationRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Évaluation non trouvée"));

		if ("submitted".equals(evaluation.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cette évaluation a déjà été soumise");
		}

		DefenseSession ds = defenseSessionRepository.findById(evaluation.getDefenseSessionId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Session de soutenance non trouvée"));

		if (ds.getSubmissionDeadline() != null && LocalDate.now().isAfter(ds.getSubmissionDeadline())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"La date limite de soumission des évaluations est dépassée");
		}

		if (request.score() != null)
			evaluation.setScore(request.score());
		if (request.comment() != null)
			evaluation.setComment(request.comment());

		evaluation.setStatus("submitted");
		evaluation.setSubmittedAt(LocalDateTime.now());
		return toResponse(evaluationRepository.save(evaluation));
	}

	private Map<String, Object> toResponse(Evaluation evaluation) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", evaluation.getId());
		map.put("defenseId", evaluation.getDefenseSessionId());
		map.put("role", evaluation.getRole());
		map.put("score", evaluation.getScore());
		map.put("comment", evaluation.getComment());
		map.put("status", evaluation.getStatus());
		map.put("submittedAt", evaluation.getSubmittedAt());

		Project project = projectRepository.findById(evaluation.getProjectId()).orElse(null);
		map.put("projectTitle", project != null ? project.getTitle() : "");
		map.put("studentNames", getStudentNames(evaluation.getProjectId()));

		return map;
	}

	private List<String> getStudentNames(Long projectId) {
		List<Group> groups = groupRepository.findByProjectId(projectId);
		for (Group g : groups) {
			if (g.getStudents() != null && !g.getStudents().isEmpty()) {
				return g.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName())
						.collect(Collectors.toList());
			}
		}
		Project project = projectRepository.findById(projectId).orElse(null);
		if (project != null && project.getStudents() != null) {
			return project.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName())
					.collect(Collectors.toList());
		}
		return List.of();
	}
}
