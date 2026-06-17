package com.system_gestion_soutenance.api.coordinator.grade.service;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.grade.dto.DeliberationRequest;
import com.system_gestion_soutenance.api.coordinator.grade.dto.DeliberationStateResponse;
import com.system_gestion_soutenance.api.coordinator.grade.dto.DeliberationStateResponse.DefenseDeliberationDetails;
import com.system_gestion_soutenance.api.coordinator.grade.dto.ScoreAdjustRequest;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
public class CoordinatorDeliberationService {

	private final DefenseSessionRepository defenseSessionRepository;
	private final DefenseRepository defenseRepository;
	private final SecurityService securityService;

	public CoordinatorDeliberationService(DefenseSessionRepository defenseSessionRepository,
			DefenseRepository defenseRepository, SecurityService securityService) {
		this.defenseSessionRepository = defenseSessionRepository;
		this.defenseRepository = defenseRepository;
		this.securityService = securityService;
	}

	@Transactional(readOnly = true)
	public DeliberationStateResponse getDeliberationState(Long sessionId) {
		DefenseSession session = defenseSessionRepository.findById(sessionId)
				.orElseThrow(() -> new EntityNotFoundException("Session non trouvée"));

		List<Defense> defenses = defenseRepository.findAllWithMembers();
		List<DefenseDeliberationDetails> details = new ArrayList<>();
		for (Defense d : defenses) {
			if (d.getProject() == null)
				continue;
			details.add(new DefenseDeliberationDetails(d.getProject().getId(), d.getProject().getTitle(),
					d.getFinalScore(), d.getMention(), d.getDeliberationComment()));
		}

		return new DeliberationStateResponse(session.getId(), session.getName(), session.getDeliberatedBy(),
				session.getDeliberatedAt(), session.getValidatedBy(), session.getValidatedAt(),
				session.isResultsPublished(), details);
	}

	@Transactional
	public DeliberationStateResponse deliberate(Long sessionId, DeliberationRequest request) {
		DefenseSession session = defenseSessionRepository.findById(sessionId)
				.orElseThrow(() -> new EntityNotFoundException("Session non trouvée"));

		if (session.getDeliberatedAt() != null) {
			throw new InvalidBusinessStateException("La délibération a déjà été finalisée");
		}

		Long currentUserId = securityService.getCurrentUserId();
		List<Defense> defenses = defenseRepository.findAllWithMembers();

		for (Defense d : defenses) {
			if (d.getProject() == null)
				continue;
			Double score = request.finalScores().get(d.getProject().getId());
			if (score != null) {
				d.setFinalScore(score);
				d.setMention(resolveMention(score));
				d.setDeliberationComment(request.comment());
			}
		}

		session.setDeliberatedBy(currentUserId);
		session.setDeliberatedAt(LocalDateTime.now());

		defenseRepository.saveAll(defenses);
		defenseSessionRepository.save(session);

		return getDeliberationState(sessionId);
	}

	@Transactional
	public void adjustScore(Long defenseId, ScoreAdjustRequest request) {
		Defense defense = defenseRepository.findById(defenseId)
				.orElseThrow(() -> new EntityNotFoundException("Soutenance non trouvée"));

		defense.setFinalScore(request.finalScore());
		defense.setMention(resolveMention(request.finalScore()));
		if (request.comment() != null) {
			defense.setDeliberationComment(request.comment());
		}
		defenseRepository.save(defense);
	}

	private String resolveMention(double score) {
		if (score >= 16)
			return "TRES_BIEN";
		if (score >= 14)
			return "BIEN";
		if (score >= 12)
			return "ASSEZ_BIEN";
		if (score >= 10)
			return "PASSABLE";
		return "INSuffisant";
	}
}
