package com.system_gestion_soutenance.api.coordinator.defensesession.service;

import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.TemplateRole;
import com.system_gestion_soutenance.api.admin.config.juryrole.repository.JuryRoleTemplateRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSessionStatus;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseType;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.defensesession.dto.CreateDefenseSessionRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CoordinatorDefenseSessionService {

	private static final Map<DefenseSessionStatus, Set<DefenseSessionStatus>> VALID_TRANSITIONS = Map.of(
			DefenseSessionStatus.DRAFT, Set.of(DefenseSessionStatus.ACTIVE, DefenseSessionStatus.SCHEDULED),
			DefenseSessionStatus.ACTIVE, Set.of(DefenseSessionStatus.SCHEDULED, DefenseSessionStatus.COMPLETED),
			DefenseSessionStatus.SCHEDULED, Set.of(DefenseSessionStatus.COMPLETED), DefenseSessionStatus.COMPLETED,
			Set.of(DefenseSessionStatus.ARCHIVED), DefenseSessionStatus.ARCHIVED, Set.of());

	private final DefenseSessionRepository defenseSessionRepository;
	private final JuryRoleTemplateRepository juryRoleTemplateRepository;

	public CoordinatorDefenseSessionService(DefenseSessionRepository defenseSessionRepository,
			JuryRoleTemplateRepository juryRoleTemplateRepository) {
		this.defenseSessionRepository = defenseSessionRepository;
		this.juryRoleTemplateRepository = juryRoleTemplateRepository;
	}

	public List<DefenseSession> findAll() {
		return defenseSessionRepository.findAll();
	}

	@Transactional
	public DefenseSession create(CreateDefenseSessionRequest request) {
		DefenseSession ds = new DefenseSession();
		ds.setName(request.name());
		ds.setDefenseType(parseDefenseType(request.defenseType()));
		ds.setStatus(request.status() != null ? parseStatus(request.status()) : DefenseSessionStatus.DRAFT);
		ds.setMaxGroupSize(request.maxGroupSize());
		ds.setDefenseDuration(request.defenseDuration());
		ds.setBreakDuration(request.breakDuration());
		ds.setSubmissionDeadline(
				request.submissionDeadline() != null ? LocalDate.parse(request.submissionDeadline()) : null);
		ds.setStartDate(LocalDate.parse(request.startDate()));
		ds.setEndDate(LocalDate.parse(request.endDate()));

		if (request.juryRoleTemplateId() != null) {
			JuryRoleTemplate template = juryRoleTemplateRepository.findById(request.juryRoleTemplateId())
					.orElseThrow(() -> new InvalidBusinessStateException("Template de rôle jury introuvable"));
			ds.setJuryRoleTemplate(template);
		}

		if (request.evaluationCoefficients() != null) {
			ds.setEvaluationCoefficients(new HashMap<>(request.evaluationCoefficients()));
		} else if (request.juryRoleTemplateId() != null && ds.getJuryRoleTemplate() != null
				&& ds.getJuryRoleTemplate().getRoles() != null) {
			ds.setEvaluationCoefficients(ds.getJuryRoleTemplate().getRoles().stream()
					.collect(Collectors.toMap(TemplateRole::getName, TemplateRole::getCoefficient)));
		}

		return defenseSessionRepository.save(ds);
	}

	@Transactional
	public DefenseSession update(Long id, CreateDefenseSessionRequest request) {
		DefenseSession ds = defenseSessionRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));

		DefenseSessionStatus newStatus = request.status() != null ? parseStatus(request.status()) : ds.getStatus();
		validateTransition(ds.getStatus(), newStatus);

		ds.setName(request.name());
		ds.setDefenseType(parseDefenseType(request.defenseType()));
		ds.setStatus(newStatus);
		ds.setMaxGroupSize(request.maxGroupSize());
		ds.setDefenseDuration(request.defenseDuration());
		ds.setBreakDuration(request.breakDuration());
		ds.setSubmissionDeadline(
				request.submissionDeadline() != null ? LocalDate.parse(request.submissionDeadline()) : null);
		ds.setStartDate(LocalDate.parse(request.startDate()));
		ds.setEndDate(LocalDate.parse(request.endDate()));

		if (request.juryRoleTemplateId() != null) {
			JuryRoleTemplate template = juryRoleTemplateRepository.findById(request.juryRoleTemplateId())
					.orElseThrow(() -> new InvalidBusinessStateException("Template de rôle jury introuvable"));
			ds.setJuryRoleTemplate(template);
		} else {
			ds.setJuryRoleTemplate(null);
		}

		if (request.evaluationCoefficients() != null) {
			ds.setEvaluationCoefficients(new HashMap<>(request.evaluationCoefficients()));
		} else if (request.juryRoleTemplateId() != null && ds.getJuryRoleTemplate() != null
				&& ds.getJuryRoleTemplate().getRoles() != null) {
			ds.setEvaluationCoefficients(ds.getJuryRoleTemplate().getRoles().stream()
					.collect(Collectors.toMap(TemplateRole::getName, TemplateRole::getCoefficient)));
		}

		return defenseSessionRepository.save(ds);
	}

	@Transactional
	public void delete(Long id) {
		if (!defenseSessionRepository.existsById(id)) {
			throw new EntityNotFoundException("Session de soutenance non trouvée");
		}
		defenseSessionRepository.deleteById(id);
	}

	@Transactional
	public DefenseSession transition(Long id, String toStatus) {
		DefenseSession ds = defenseSessionRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));

		DefenseSessionStatus newStatus = parseStatus(toStatus);
		validateTransition(ds.getStatus(), newStatus);
		ds.setStatus(newStatus);
		return defenseSessionRepository.save(ds);
	}

	private void validateTransition(DefenseSessionStatus from, DefenseSessionStatus to) {
		if (from == to)
			return;
		Set<DefenseSessionStatus> allowed = VALID_TRANSITIONS.get(from);
		if (allowed == null || !allowed.contains(to)) {
			throw new InvalidBusinessStateException("Transition de statut invalide: " + from + " → " + to);
		}
	}

	private DefenseSessionStatus parseStatus(String status) {
		try {
			return DefenseSessionStatus.valueOf(status.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new InvalidBusinessStateException(
					"Statut invalide. Valeurs autorisées: DRAFT, ACTIVE, SCHEDULED, COMPLETED, ARCHIVED");
		}
	}

	private DefenseType parseDefenseType(String type) {
		try {
			return DefenseType.valueOf(type.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new InvalidBusinessStateException(
					"Type de soutenance invalide. Valeurs autorisées: PFE, MEMOIRE, THESE");
		}
	}
}
