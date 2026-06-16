package com.system_gestion_soutenance.api.notification.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.entity.NotificationType;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

	@Mock
	private NotificationRepository notificationRepository;

	@InjectMocks
	private NotificationEventListener listener;

	@Test
	void handleDomainEvent_withProjectProposedEvent_createsInfoNotification() {
		ProjectProposedEvent event = new ProjectProposedEvent("admin@test.com", 1L, "Projet PFE", "Alice");

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		AppNotification n = captor.getValue();
		assertEquals(NotificationType.INFO, n.getType());
		assertEquals("Nouveau projet proposé", n.getTitle());
		assertTrue(n.getMessage().contains("Projet PFE"));
		assertEquals("/coordinator/projects", n.getActionLink());
		assertEquals("admin@test.com", n.getActor());
		assertFalse(n.isRead());
	}

	@Test
	void handleDomainEvent_withProjectStatusChangedApproved_createsSuccessNotification() {
		ProjectStatusChangedEvent event = new ProjectStatusChangedEvent("admin@test.com", 1L, "Projet", "PENDING",
				"APPROVED");

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		assertEquals(NotificationType.SUCCESS, captor.getValue().getType());
	}

	@Test
	void handleDomainEvent_withProjectStatusChangedRejected_createsWarningNotification() {
		ProjectStatusChangedEvent event = new ProjectStatusChangedEvent("admin@test.com", 1L, "Projet", "PENDING",
				"REJECTED");

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		assertEquals(NotificationType.WARNING, captor.getValue().getType());
	}

	@Test
	void handleDomainEvent_withDefenseSessionCreatedEvent_createsInfoNotification() {
		DefenseSessionCreatedEvent event = new DefenseSessionCreatedEvent("admin@test.com", 1L, "Session 1");

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		assertEquals(NotificationType.INFO, captor.getValue().getType());
		assertEquals("Session créée", captor.getValue().getTitle());
	}

	@Test
	void handleDomainEvent_withDefenseSessionFrozenEvent_createsWarningNotification() {
		DefenseSessionFrozenEvent event = new DefenseSessionFrozenEvent("admin@test.com", 1L, "Session 1");

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		assertEquals(NotificationType.WARNING, captor.getValue().getType());
		assertEquals("Session gelée", captor.getValue().getTitle());
	}

	@Test
	void handleDomainEvent_withDefenseSessionUnfrozenEvent_createsInfoNotification() {
		DefenseSessionUnfrozenEvent event = new DefenseSessionUnfrozenEvent("admin@test.com", 1L, "Session 1");

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		assertEquals(NotificationType.INFO, captor.getValue().getType());
		assertEquals("Session dégelée", captor.getValue().getTitle());
	}

	@Test
	void handleDomainEvent_withDefenseSessionStatusChangedEvent_createsInfoNotification() {
		DefenseSessionStatusChangedEvent event = new DefenseSessionStatusChangedEvent("admin@test.com", 1L, "Session 1",
				"COMPLETED");

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		assertEquals(NotificationType.INFO, captor.getValue().getType());
		assertEquals("Statut de session modifié", captor.getValue().getTitle());
	}

	@Test
	void handleDomainEvent_withStudentLeftGroupEvent_createsInfoNotification() {
		StudentLeftGroupEvent event = new StudentLeftGroupEvent("coord@test.com", 1L, "Bob", 10L);

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		assertEquals(NotificationType.INFO, captor.getValue().getType());
		assertTrue(captor.getValue().getMessage().contains("Bob"));
	}

	@Test
	void handleDomainEvent_withStudentDocumentSubmittedEvent_createsSuccessNotification() {
		StudentDocumentSubmittedEvent event = new StudentDocumentSubmittedEvent("coord@test.com", 1L, "Rapport.pdf",
				"Alice");

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		assertEquals(NotificationType.SUCCESS, captor.getValue().getType());
		assertEquals("Document soumis", captor.getValue().getTitle());
	}

	@Test
	void handleDomainEvent_withEvaluationSubmittedEvent_createsSuccessNotification() {
		EvaluationSubmittedEvent event = new EvaluationSubmittedEvent("teacher@test.com", 1L, "Projet X", 15.5);

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		assertEquals(NotificationType.SUCCESS, captor.getValue().getType());
		assertTrue(captor.getValue().getMessage().contains("15,5") || captor.getValue().getMessage().contains("15.5"));
	}

	@Test
	void handleDomainEvent_withDefenseSessionPublishedEvent_createsSuccessNotification() {
		DefenseSessionPublishedEvent event = new DefenseSessionPublishedEvent("admin@test.com", 1L, "Session 1");

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		assertEquals(NotificationType.SUCCESS, captor.getValue().getType());
		assertEquals("Session publiée", captor.getValue().getTitle());
	}

	@Test
	void handleDomainEvent_withDefenseCancelledEventWithDateAndTime_createsWarningNotification() {
		DefenseCancelledEvent event = new DefenseCancelledEvent("coord@test.com", 1L, LocalDate.of(2025, 6, 1),
				LocalTime.of(9, 0));

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		assertEquals(NotificationType.WARNING, captor.getValue().getType());
		assertEquals("Soutenance annulée", captor.getValue().getTitle());
		assertTrue(captor.getValue().getMessage().contains("01/06/2025"));
		assertTrue(captor.getValue().getMessage().contains("09:00"));
	}

	@Test
	void handleDomainEvent_withDefenseCancelledEventWithNullDateAndTime_usesInconnue() {
		DefenseCancelledEvent event = new DefenseCancelledEvent("coord@test.com", 1L, null, null);

		listener.handleDomainEvent(event);

		ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
		verify(notificationRepository).save(captor.capture());
		assertTrue(captor.getValue().getMessage().contains("inconnue"));
	}

	@Test
	void handleDomainEvent_withUnhandledEvent_doesNotSaveNotification() {
		DomainEvent event = new DomainEvent("test") {
		};

		listener.handleDomainEvent(event);

		verify(notificationRepository, never()).save(any());
	}
}
