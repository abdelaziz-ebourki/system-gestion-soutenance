package com.system_gestion_soutenance.api.coordinator.defensesession.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.TemplateRole;
import com.system_gestion_soutenance.api.admin.config.juryrole.repository.JuryRoleTemplateRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSessionStatus;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseType;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import org.junit.jupiter.api.Test;
import com.system_gestion_soutenance.api.coordinator.defensesession.dto.CreateDefenseSessionRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;

class CoordinatorDefenseSessionServiceTest {

	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);
	private final JuryRoleTemplateRepository juryRoleTemplateRepository = mock(JuryRoleTemplateRepository.class);
	private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
	private final SecurityService securityService = mock(SecurityService.class);

	private final CoordinatorDefenseSessionService service = new CoordinatorDefenseSessionService(
			defenseSessionRepository, juryRoleTemplateRepository, eventPublisher, securityService);

	@Test
	void findAll_returnsAllSessions() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setName("Session PFE");
		when(defenseSessionRepository.findAll()).thenReturn(List.of(ds));

		var result = service.findAll();

		assertEquals(1, result.size());
		assertEquals("Session PFE", result.get(0).getName());
	}

	@Test
	void create_withValidRequest_returnsSession() {
		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Session PFE", "PFE", null, 3, 30, 15,
				null, null, null, "2025-06-01", "2025-06-30");

		DefenseSession saved = new DefenseSession();
		saved.setId(1L);
		saved.setName("Session PFE");
		when(defenseSessionRepository.save(any(DefenseSession.class))).thenReturn(saved);
		when(securityService.getCurrentUserEmail()).thenReturn("coord@test.com");

		var result = service.create(request);

		assertEquals("Session PFE", result.getName());
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void create_withTemplateAndCoefficients_usesDefaults() {
		TemplateRole role = new TemplateRole();
		role.setName("président");
		role.setCoefficient(2);
		JuryRoleTemplate template = new JuryRoleTemplate();
		template.setId(10L);
		template.setRoles(List.of(role));

		when(juryRoleTemplateRepository.findById(10L)).thenReturn(Optional.of(template));
		when(securityService.getCurrentUserEmail()).thenReturn("coord@test.com");

		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Session", "PFE", "DRAFT", 3, 30, 15,
				null, Map.of("président", 2), 10L, "2025-06-01", "2025-06-30");

		DefenseSession saved = new DefenseSession();
		saved.setId(1L);
		saved.setName("Session");
		when(defenseSessionRepository.save(any(DefenseSession.class))).thenReturn(saved);

		service.create(request);

		verify(defenseSessionRepository).save(argThat(ds -> ds.getEvaluationCoefficients() != null
				&& ds.getEvaluationCoefficients().containsKey("président") && ds.getJuryRoleTemplate() != null));
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void create_withTemplateAndNoCoefficients_usesTemplateDefaults() {
		TemplateRole role = mock(TemplateRole.class);
		when(role.getName()).thenReturn("président");
		when(role.getCoefficient()).thenReturn(2);

		JuryRoleTemplate template = mock(JuryRoleTemplate.class);
		when(template.getId()).thenReturn(10L);
		when(template.getRoles()).thenReturn(List.of(role));

		when(juryRoleTemplateRepository.findById(10L)).thenReturn(Optional.of(template));
		when(securityService.getCurrentUserEmail()).thenReturn("coord@test.com");

		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Session", "PFE", "DRAFT", 3, 30, 15,
				null, null, 10L, "2025-06-01", "2025-06-30");

		DefenseSession saved = new DefenseSession();
		saved.setId(1L);
		when(defenseSessionRepository.save(any(DefenseSession.class))).thenReturn(saved);

		service.create(request);

		verify(defenseSessionRepository).save(argThat(ds -> ds.getEvaluationCoefficients() != null
				&& ds.getEvaluationCoefficients().containsKey("président")));
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void create_invalidDefenseType_throwsException() {
		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Session", "INVALID", null, 3, 30, 15,
				null, null, null, "2025-06-01", "2025-06-30");

		assertThrows(InvalidBusinessStateException.class, () -> service.create(request));
	}

	@Test
	void update_existingSession_returnsUpdated() {
		DefenseSession existing = new DefenseSession();
		existing.setId(1L);
		existing.setName("Old");
		existing.setStatus(DefenseSessionStatus.DRAFT);
		existing.setDefenseType(DefenseType.PFE);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(existing));

		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Updated", "MEMOIRE", null, 4, 20, 10,
				null, null, null, "2025-07-01", "2025-07-31");

		when(defenseSessionRepository.save(any(DefenseSession.class))).thenReturn(existing);

		var result = service.update(1L, request);

		assertEquals("Updated", result.getName());
	}

	@Test
	void update_sessionNotFound_throwsException() {
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("X", "PFE", null, 1, 1, 1, null, null,
				null, "2025-01-01", "2025-01-31");

		assertThrows(EntityNotFoundException.class, () -> service.update(99L, request));
	}

	@Test
	void delete_existingSession_deletes() {
		when(defenseSessionRepository.existsById(1L)).thenReturn(true);

		service.delete(1L);

		verify(defenseSessionRepository).deleteById(1L);
	}

	@Test
	void create_withNullStatusAndSubmissionDeadline_setsDefaults() {
		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Session", "PFE", null, 3, 30, 15, null,
				null, null, "2025-06-01", "2025-06-30");

		DefenseSession saved = new DefenseSession();
		saved.setId(1L);
		when(defenseSessionRepository.save(any(DefenseSession.class))).thenReturn(saved);

		var result = service.create(request);

		verify(defenseSessionRepository).save(
				argThat(ds -> ds.getStatus() == DefenseSessionStatus.DRAFT && ds.getSubmissionDeadline() == null));
		assertEquals(1L, result.getId());
	}

	@Test
	void update_withNullStatus_keepsExistingStatus() {
		DefenseSession existing = new DefenseSession();
		existing.setId(1L);
		existing.setName("Old");
		existing.setStatus(DefenseSessionStatus.DRAFT);
		existing.setDefenseType(DefenseType.PFE);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(existing));

		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Updated", "MEMOIRE", null, 4, 20, 10,
				null, null, null, "2025-07-01", "2025-07-31");

		when(defenseSessionRepository.save(existing)).thenReturn(existing);

		service.update(1L, request);

		assertEquals(DefenseSessionStatus.DRAFT, existing.getStatus());
	}

	@Test
	void update_withTemplate_updatesTemplate() {
		DefenseSession existing = new DefenseSession();
		existing.setId(1L);
		existing.setName("Old");
		existing.setStatus(DefenseSessionStatus.DRAFT);
		existing.setDefenseType(DefenseType.PFE);

		TemplateRole role = new TemplateRole();
		role.setName("président");
		role.setCoefficient(2);

		JuryRoleTemplate template = new JuryRoleTemplate();
		template.setId(10L);
		template.setRoles(List.of(role));

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(juryRoleTemplateRepository.findById(10L)).thenReturn(Optional.of(template));
		when(defenseSessionRepository.save(existing)).thenReturn(existing);

		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Updated", "MEMOIRE", null, 4, 20, 10,
				null, null, 10L, "2025-07-01", "2025-07-31");

		service.update(1L, request);

		assertEquals(template, existing.getJuryRoleTemplate());
	}

	@Test
	void update_withTemplateAndNoCoefficients_usesTemplateDefaults() {
		DefenseSession existing = new DefenseSession();
		existing.setId(1L);
		existing.setName("Old");
		existing.setStatus(DefenseSessionStatus.DRAFT);
		existing.setDefenseType(DefenseType.PFE);

		TemplateRole role = mock(TemplateRole.class);
		when(role.getName()).thenReturn("président");
		when(role.getCoefficient()).thenReturn(2);

		JuryRoleTemplate template = mock(JuryRoleTemplate.class);
		when(template.getId()).thenReturn(10L);
		when(template.getRoles()).thenReturn(List.of(role));

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(juryRoleTemplateRepository.findById(10L)).thenReturn(Optional.of(template));
		when(defenseSessionRepository.save(existing)).thenReturn(existing);

		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Updated", "MEMOIRE", null, 4, 20, 10,
				null, null, 10L, "2025-07-01", "2025-07-31");

		service.update(1L, request);

		verify(defenseSessionRepository).save(argThat(ds -> ds.getEvaluationCoefficients() != null
				&& ds.getEvaluationCoefficients().containsKey("président")));
	}

	@Test
	void transition_validTransition_updatesStatus() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setName("Session");
		ds.setStatus(DefenseSessionStatus.DRAFT);
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSessionRepository.save(ds)).thenReturn(ds);
		when(securityService.getCurrentUserEmail()).thenReturn("coord@test.com");

		var result = service.transition(1L, "ACTIVE");
		assertEquals(DefenseSessionStatus.ACTIVE, result.getStatus());
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void delete_sessionNotFound_throwsException() {
		when(defenseSessionRepository.existsById(99L)).thenReturn(false);

		assertThrows(EntityNotFoundException.class, () -> service.delete(99L));
	}

	@Test
	void transition_activeToScheduled_success() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setName("Session");
		ds.setStatus(DefenseSessionStatus.ACTIVE);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSessionRepository.save(ds)).thenReturn(ds);
		when(securityService.getCurrentUserEmail()).thenReturn("coord@test.com");

		var result = service.transition(1L, "SCHEDULED");

		assertEquals(DefenseSessionStatus.SCHEDULED, result.getStatus());
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void transition_scheduledToCompleted_success() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setName("Session");
		ds.setStatus(DefenseSessionStatus.SCHEDULED);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSessionRepository.save(ds)).thenReturn(ds);
		when(securityService.getCurrentUserEmail()).thenReturn("coord@test.com");

		var result = service.transition(1L, "COMPLETED");

		assertEquals(DefenseSessionStatus.COMPLETED, result.getStatus());
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void transition_completedToArchived_success() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setName("Session");
		ds.setStatus(DefenseSessionStatus.COMPLETED);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSessionRepository.save(ds)).thenReturn(ds);
		when(securityService.getCurrentUserEmail()).thenReturn("coord@test.com");

		var result = service.transition(1L, "ARCHIVED");

		assertEquals(DefenseSessionStatus.ARCHIVED, result.getStatus());
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void transition_invalidTransition_throwsException() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setStatus(DefenseSessionStatus.DRAFT);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		assertThrows(InvalidBusinessStateException.class, () -> service.transition(1L, "ARCHIVED"));
	}

	@Test
	void transition_sameStatus_isAllowed() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setStatus(DefenseSessionStatus.DRAFT);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSessionRepository.save(ds)).thenReturn(ds);

		var result = service.transition(1L, "DRAFT");

		assertEquals(DefenseSessionStatus.DRAFT, result.getStatus());
	}

	@Test
	void transition_sessionNotFound_throwsException() {
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.transition(99L, "ACTIVE"));
	}

	@Test
	void transition_invalidStatusString_throwsException() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setStatus(DefenseSessionStatus.DRAFT);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		assertThrows(InvalidBusinessStateException.class, () -> service.transition(1L, "INVALID_STATUS"));
	}

	@Test
	void transition_toCompleted_autoFreezes() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setStatus(DefenseSessionStatus.ACTIVE);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSessionRepository.save(ds)).thenReturn(ds);

		var result = service.transition(1L, "COMPLETED");

		assertEquals(DefenseSessionStatus.COMPLETED, result.getStatus());
		assertTrue(result.isFrozen());
	}

	@Test
	void freeze_setsIsFrozenTrue() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setName("Session");
		ds.setFrozen(false);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSessionRepository.save(ds)).thenReturn(ds);
		when(securityService.getCurrentUserEmail()).thenReturn("coord@test.com");

		var result = service.freeze(1L);

		assertTrue(result.isFrozen());
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void unfreeze_setsIsFrozenFalse() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setName("Session");
		ds.setFrozen(true);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSessionRepository.save(ds)).thenReturn(ds);
		when(securityService.getCurrentUserEmail()).thenReturn("coord@test.com");

		var result = service.unfreeze(1L);

		assertFalse(result.isFrozen());
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void freeze_sessionNotFound_throws() {
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.freeze(99L));
	}

	@Test
	void unfreeze_sessionNotFound_throws() {
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.unfreeze(99L));
	}

	@Test
	void unfreeze_completedSession_throws() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setFrozen(true);
		ds.setStatus(DefenseSessionStatus.COMPLETED);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		assertThrows(InvalidBusinessStateException.class, () -> service.unfreeze(1L));
		verify(defenseSessionRepository, never()).save(any());
	}

	@Test
	void approve_setsApprovedByAndTimestamp() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setApprovedBy(null);
		ds.setApprovedAt(null);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSessionRepository.save(ds)).thenReturn(ds);

		var result = service.approve(1L, 10L);

		assertEquals(10L, result.getApprovedBy());
		assertNotNull(result.getApprovedAt());
		verify(defenseSessionRepository).save(ds);
	}

	@Test
	void approve_sessionNotFound_throws() {
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.approve(99L, 10L));
	}

	@Test
	void revokeApproval_clearsApprovedByAndTimestamp() {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setApprovedBy(10L);
		ds.setApprovedAt(java.time.LocalDateTime.now());

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSessionRepository.save(ds)).thenReturn(ds);

		var result = service.revokeApproval(1L);

		assertNull(result.getApprovedBy());
		assertNull(result.getApprovedAt());
		verify(defenseSessionRepository).save(ds);
	}

	@Test
	void revokeApproval_sessionNotFound_throws() {
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.revokeApproval(99L));
	}
}
