package com.system_gestion_soutenance.api.coordinator.defensesession.service;

import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.TemplateRole;
import com.system_gestion_soutenance.api.admin.config.juryrole.repository.JuryRoleTemplateRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSessionStatus;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseType;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.defensesession.dto.CreateDefenseSessionRequest;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.notification.event.DefenseSessionCreatedEvent;
import com.system_gestion_soutenance.api.notification.event.DefenseSessionFrozenEvent;
import com.system_gestion_soutenance.api.notification.event.DefenseSessionStatusChangedEvent;
import com.system_gestion_soutenance.api.notification.event.DefenseSessionUnfrozenEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDateTime;
@SuppressWarnings("PMD")

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
	private final ApplicationEventPublisher eventPublisher;
	private final SecurityService securityService;

	public CoordinatorDefenseSessionService(DefenseSessionRepository defenseSessionRepository,
			JuryRoleTemplateRepository juryRoleTemplateRepository, ApplicationEventPublisher eventPublisher,
			SecurityService securityService) {
		this.defenseSessionRepository = defenseSessionRepository;
		this.juryRoleTemplateRepository = juryRoleTemplateRepository;
		this.eventPublisher = eventPublisher;
		this.securityService = securityService;
	}

	public List<DefenseSession> findAll() {
		return defenseSessionRepository.findAll();
	}

	public PaginatedResponse<DefenseSession> findAll(int page, int limit) {
		Page<DefenseSession> dsPage = defenseSessionRepository.findAll(PageRequest.of(page, limit));
		return new PaginatedResponse<>(dsPage.getContent(), dsPage.getTotalElements(), dsPage.getTotalPages(), page,
				limit);
	}

	@Transactional
	public DefenseSession create(CreateDefenseSessionRequest request) {
		if (request.maxGroupSize() < 1) {
			throw new InvalidBusinessStateException("La taille maximale du groupe doit être au moins 1");
		}

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

		DefenseSession saved = defenseSessionRepository.save(ds);
		eventPublisher.publishEvent(
				new DefenseSessionCreatedEvent(securityService.getCurrentUserEmail(), saved.getId(), saved.getName()));
		return saved;
	}

	@Transactional
	public DefenseSession update(Long id, CreateDefenseSessionRequest request) {
		if (request.maxGroupSize() < 1) {
			throw new InvalidBusinessStateException("La taille maximale du groupe doit être au moins 1");
		}

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
		if (newStatus == DefenseSessionStatus.COMPLETED) {
			ds.setFrozen(true);
		}
		DefenseSession saved = defenseSessionRepository.save(ds);
		eventPublisher.publishEvent(new DefenseSessionStatusChangedEvent(securityService.getCurrentUserEmail(),
				saved.getId(), saved.getName(), newStatus.name()));
		return saved;
	}

	@Transactional
	public DefenseSession freeze(Long id) {
		DefenseSession ds = defenseSessionRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));
		ds.setFrozen(true);
		DefenseSession saved = defenseSessionRepository.save(ds);
		eventPublisher.publishEvent(
				new DefenseSessionFrozenEvent(securityService.getCurrentUserEmail(), saved.getId(), saved.getName()));
		return saved;
	}

	@Transactional
	public DefenseSession unfreeze(Long id) {
		DefenseSession ds = defenseSessionRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));
		if (ds.getStatus() == DefenseSessionStatus.COMPLETED) {
			throw new InvalidBusinessStateException("Impossible de dégeler une session terminée");
		}
		ds.setFrozen(false);
		DefenseSession saved = defenseSessionRepository.save(ds);
		eventPublisher.publishEvent(
				new DefenseSessionUnfrozenEvent(securityService.getCurrentUserEmail(), saved.getId(), saved.getName()));
		return saved;
	}

	@Transactional
	public DefenseSession approve(Long id, Long adminUserId) {
		DefenseSession ds = defenseSessionRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));
		ds.setApprovedBy(adminUserId);
		ds.setApprovedAt(LocalDateTime.now());
		return defenseSessionRepository.save(ds);
	}

	@Transactional
	public DefenseSession revokeApproval(Long id) {
		DefenseSession ds = defenseSessionRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));
		ds.setApprovedBy(null);
		ds.setApprovedAt(null);
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