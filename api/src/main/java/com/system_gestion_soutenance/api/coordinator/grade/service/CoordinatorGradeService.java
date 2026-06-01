package com.system_gestion_soutenance.api.coordinator.grade.service;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoordinatorGradeService {

	private final JuryRepository juryRepository;
	private final EvaluationRepository evaluationRepository;
	private final DefenseSessionRepository defenseSessionRepository;
	private final GroupRepository groupRepository;
	private final SlotAssignmentRepository slotAssignmentRepository;

	public CoordinatorGradeService(JuryRepository juryRepository, EvaluationRepository evaluationRepository,
			DefenseSessionRepository defenseSessionRepository, GroupRepository groupRepository,
			SlotAssignmentRepository slotAssignmentRepository) {
		this.juryRepository = juryRepository;
		this.evaluationRepository = evaluationRepository;
		this.defenseSessionRepository = defenseSessionRepository;
		this.groupRepository = groupRepository;
		this.slotAssignmentRepository = slotAssignmentRepository;
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getGrades() {
		List<Jury> juries = juryRepository.findAllWithDetails();
		if (juries.isEmpty())
			return List.of();

		List<Long> projectIds = juries.stream().map(j -> j.getProject().getId()).collect(Collectors.toList());

		List<Evaluation> allEvaluations = evaluationRepository.findByProjectIdIn(projectIds);
		Map<Long, List<Evaluation>> evaluationsByProject = allEvaluations != null
				? allEvaluations.stream().collect(Collectors.groupingBy(Evaluation::getProjectId))
				: Map.of();

		List<SlotAssignment> allSlots = slotAssignmentRepository.findByProjectIdIn(projectIds);
		Map<Long, String> datesByProject = allSlots != null
				? allSlots.stream()
						.collect(Collectors.toMap(SlotAssignment::getProjectId, SlotAssignment::getDate, (a, b) -> a))
				: Map.of();

		Map<Long, Long> sessionIdsByProject = new HashMap<>();
		Set<Long> sessionIdsToFetch = new HashSet<>();

		for (Jury jury : juries) {
			Long pid = jury.getProject().getId();
			List<Evaluation> evals = evaluationsByProject.getOrDefault(pid, List.of());
			Long sid = resolveDefenseSessionId(pid, evals);
			if (sid != null) {
				sessionIdsByProject.put(pid, sid);
				sessionIdsToFetch.add(sid);
			}
		}

		Map<Long, Map<String, Integer>> coefficientsBySession = defenseSessionRepository.findAllById(sessionIdsToFetch)
				.stream().collect(Collectors.toMap(DefenseSession::getId, DefenseSession::getEvaluationCoefficients));

		List<Map<String, Object>> grades = new ArrayList<>();
		for (Jury jury : juries) {
			Long projectId = jury.getProject().getId();
			List<Evaluation> evaluations = evaluationsByProject.getOrDefault(projectId, List.of());
			System.out.println("Project: " + projectId + ", Evaluations: " + evaluations.size());

			Long defenseSessionId = sessionIdsByProject.get(projectId);
			Map<String, Integer> coefficients = coefficientsBySession.getOrDefault(defenseSessionId, Map.of());
			System.out.println("Session: " + defenseSessionId + ", Coefficients: " + coefficients);

			String defenseDate = datesByProject.get(projectId);

			List<Map<String, Object>> individualScores = buildIndividualScores(jury.getMembers(), evaluations);

			String status = computeStatus(evaluations, jury.getMembers().size());
			Double finalScore = status.equals("completed")
					? computeWeightedScore(jury.getMembers(), evaluations, coefficients)
					: null;
			System.out.println("Status: " + status + ", Final Score: " + finalScore);

			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("projectId", projectId);
			entry.put("projectTitle", jury.getProject().getTitle());
			entry.put("defenseDate", defenseDate);
			entry.put("status", status);
			entry.put("finalScore", finalScore);
			entry.put("evaluationCoefficients", coefficients);
			entry.put("individualScores", individualScores);
			grades.add(entry);
		}

		return grades;
	}

	private Long resolveDefenseSessionId(Long projectId, List<Evaluation> evaluations) {
		if (!evaluations.isEmpty()) {
			return evaluations.get(0).getDefenseSessionId();
		}
		return groupRepository.findByProjectId(projectId).stream().map(Group::getSessionId).filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	private List<Map<String, Object>> buildIndividualScores(List<JuryMember> members, List<Evaluation> evaluations) {
		List<Map<String, Object>> scores = new ArrayList<>();
		for (JuryMember member : members) {
			Evaluation eval = evaluations.stream().filter(e -> e.getTeacherId().equals(member.getTeacher().getId()))
					.findFirst().orElse(null);

			Map<String, Object> s = new LinkedHashMap<>();
			s.put("roleName", member.getRoleName());
			s.put("teacherName", member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName());
			s.put("score", eval != null ? eval.getScore() : null);
			scores.add(s);
		}
		return scores;
	}

	private String computeStatus(List<Evaluation> evaluations, int totalMembers) {
		long submittedCount = evaluations.stream()
				.filter(e -> "submitted".equals(e.getStatus()) && e.getScore() != null).count();
		if (submittedCount == 0)
			return "no_evaluations";
		if (submittedCount < totalMembers)
			return "pending";
		return "completed";
	}

	private Double computeWeightedScore(List<JuryMember> members, List<Evaluation> evaluations,
			Map<String, Integer> coefficients) {
		double weightedSum = 0;
		int totalCoefficient = 0;

		for (JuryMember member : members) {
			Evaluation eval = evaluations.stream()
					.filter(e -> e.getTeacherId().equals(member.getTeacher().getId()) && e.getScore() != null)
					.findFirst().orElse(null);
			if (eval != null) {
				int coeff = coefficients.getOrDefault(member.getRoleName(), 0);
				weightedSum += eval.getScore() * coeff;
				totalCoefficient += coeff;
			}
		}

		if (totalCoefficient == 0)
			return null;
		return Math.round(weightedSum / totalCoefficient * 100.0) / 100.0;
	}
}
