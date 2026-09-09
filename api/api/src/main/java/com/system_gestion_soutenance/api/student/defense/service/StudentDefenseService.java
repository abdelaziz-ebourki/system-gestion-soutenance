package com.system_gestion_soutenance.api.student.defense.service;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.student.defense.dto.JuryMemberResponse;
import com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse;
import com.system_gestion_soutenance.api.student.defense.dto.StudentGradeResponse;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationStatus;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import java.util.*;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.ResultsNotPublishedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
public class StudentDefenseService {

	private final GroupRepository groupRepository;
	private final DefenseRepository defenseRepository;
	private final EvaluationRepository evaluationRepository;

	public StudentDefenseService(GroupRepository groupRepository, DefenseRepository defenseRepository,
			EvaluationRepository evaluationRepository) {
		this.groupRepository = groupRepository;
		this.defenseRepository = defenseRepository;
		this.evaluationRepository = evaluationRepository;
	}

	@Transactional(readOnly = true)
	public StudentDefenseResponse getDefense(Long studentId) {
		Group group = groupRepository.findFirstByStudentsIdOrderByIdAsc(studentId)
				.orElseThrow(() -> new EntityNotFoundException("Aucune soutenance trouvée pour cet étudiant"));

		if (group.getProject() == null) {
			throw new EntityNotFoundException("Aucun projet associé à ce groupe");
		}

		Project project = group.getProject();

		Optional<Defense> defenseOpt = defenseRepository.findByProject(project);

		List<JuryMemberResponse> juryMembers = new ArrayList<>();
		defenseOpt.ifPresent(defense -> {
			for (JuryMember member : defense.getMembers()) {
				if (member.getTeacher() != null) {
					juryMembers.add(new JuryMemberResponse(
							member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName(),
							member.getRoleName()));
				}
			}
		});

		String date = null;
		String startTime = null;
		String roomName = null;
		String status = "pending";

		if (defenseOpt.isPresent()) {
			Defense defense = defenseOpt.get();
			date = defense.getDate().toString();
			startTime = defense.getTime().toString();
			roomName = defense.getRoom() != null ? defense.getRoom().getName() : "";
			status = "scheduled";
		}

		return new StudentDefenseResponse(project.getTitle(), project.getDescription(),
				project.getSupervisor() != null
						? project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName()
						: null,
				juryMembers, date, startTime, "", roomName, status, null, null);
	}

	@Transactional(readOnly = true)
	public StudentGradeResponse getGrade(Long studentId) {
		Group group = groupRepository.findFirstByStudentsIdOrderByIdAsc(studentId)
				.orElseThrow(() -> new EntityNotFoundException("Aucune soutenance trouvée pour cet étudiant"));

		if (group.getDefenseSession() == null) {
			throw new EntityNotFoundException("Aucune session de soutenance associée");
		}

		if (!group.getDefenseSession().isResultsPublished()) {
			throw new ResultsNotPublishedException("Les résultats ne sont pas encore publiés");
		}

		if (group.getProject() == null) {
			throw new EntityNotFoundException("Aucun projet associé à ce groupe");
		}

		Optional<Defense> defenseOpt = defenseRepository.findByProject(group.getProject());
		if (defenseOpt.isEmpty()) {
			throw new EntityNotFoundException("Aucune soutenance trouvée pour ce projet");
		}

		Defense defense = defenseOpt.get();
		List<Evaluation> evaluations = evaluationRepository.findByDefense(defense);
		Map<String, Integer> coefficients = group.getDefenseSession().getEvaluationCoefficients();

		double weightedSum = 0;
		double totalWeight = 0;

		for (Evaluation eval : evaluations) {
			if (eval.getStatus() == EvaluationStatus.SUBMITTED && eval.getScore() != null) {
				int coeff = coefficients.getOrDefault(eval.getRole(), 1);
				weightedSum += eval.getScore() * coeff;
				totalWeight += coeff;
			}
		}

		double score = totalWeight > 0 ? weightedSum / totalWeight : 0;
		String mention = computeMention(score);

		return new StudentGradeResponse(Math.round(score * 100.0) / 100.0, mention);
	}

	private String computeMention(double score) {
		if (score >= 16)
			return "Très bien";
		if (score >= 14)
			return "Bien";
		if (score >= 12)
			return "Assez bien";
		if (score >= 10)
			return "Passable";
		return "Insuffisant";
	}
}