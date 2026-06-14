package com.system_gestion_soutenance.api.notification.event;

import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.entity.NotificationType;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

	private final NotificationRepository notificationRepository;

	@EventListener
	@Transactional
	public void handleDomainEvent(DomainEvent event) {
		AppNotification notification = null;

		if (event instanceof ProjectProposedEvent e) {
			notification = createNotification(NotificationType.INFO, "Nouveau projet proposé",
					String.format("Le projet '%s' a été proposé par %s", e.getProjectTitle(), e.getStudentName()),
					"/coordinator/projects", e.getActor());
		} else if (event instanceof ProjectStatusChangedEvent e) {
			NotificationType type = e.getNewStatus().equalsIgnoreCase("APPROVED")
					? NotificationType.SUCCESS
					: NotificationType.WARNING;
			notification = createNotification(
					type, "Statut du projet mis à jour", String.format("Le projet '%s' est passé de %s à %s",
							e.getProjectTitle(), e.getOldStatus(), e.getNewStatus()),
					"/coordinator/projects", e.getActor());
		} else if (event instanceof DefenseSessionCreatedEvent e) {
			notification = createNotification(NotificationType.INFO, "Session créée",
					String.format("La session '%s' a été créée", e.getSessionName()), "/admin/sessions", e.getActor());
		} else if (event instanceof DefenseSessionFrozenEvent e) {
			notification = createNotification(NotificationType.WARNING, "Session gelée",
					String.format("La session '%s' a été gelée. Les notes ne peuvent plus être modifiées.",
							e.getSessionName()),
					"/admin/sessions", e.getActor());
		} else if (event instanceof DefenseSessionUnfrozenEvent e) {
			notification = createNotification(NotificationType.INFO, "Session dégelée",
					String.format("La session '%s' a été dégelée.", e.getSessionName()), "/admin/sessions",
					e.getActor());
		} else if (event instanceof DefenseSessionStatusChangedEvent e) {
			notification = createNotification(NotificationType.INFO, "Statut de session modifié",
					String.format("La session '%s' est maintenant %s", e.getSessionName(), e.getNewStatus()),
					"/admin/sessions", e.getActor());
		} else if (event instanceof StudentLeftGroupEvent e) {
			notification = createNotification(NotificationType.INFO, "Étudiant a quitté le groupe",
					String.format("L'étudiant %s a quitté le groupe %d", e.getStudentName(), e.getGroupId()),
					"/coordinator/groups", e.getActor());
		} else if (event instanceof StudentDocumentSubmittedEvent e) {
			notification = createNotification(NotificationType.SUCCESS, "Document soumis",
					String.format("Le document '%s' a été soumis par %s", e.getDocumentName(), e.getStudentName()),
					"/coordinator/documents", e.getActor());
		} else if (event instanceof EvaluationSubmittedEvent e) {
			notification = createNotification(NotificationType.SUCCESS, "Évaluation soumise",
					String.format("L'évaluation du projet '%s' a été soumise (Note: %.1f/20)", e.getProjectTitle(),
							e.getScore()),
					"/teacher/evaluations", e.getActor());
		} else if (event instanceof DefenseSessionPublishedEvent e) {
			notification = createNotification(NotificationType.SUCCESS, "Session publiée",
					String.format("La session '%s' a été publiée.", e.getSessionName()), "/admin/sessions",
					e.getActor());
		} else if (event instanceof DefenseCancelledEvent e) {
			String dateStr = e.getDate() != null
					? e.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
					: "inconnue";
			String timeStr = e.getTime() != null
					? e.getTime().format(DateTimeFormatter.ofPattern("HH:mm"))
					: "inconnue";
			notification = createNotification(NotificationType.WARNING, "Soutenance annulée",
					String.format("La soutenance du %s à %s a été annulée.", dateStr, timeStr), "/coordinator/schedule",
					e.getActor());
		}

		if (notification != null) {
			notificationRepository.save(notification);
		}
	}

	private AppNotification createNotification(NotificationType type, String title, String message, String actionLink,
			String actor) {
		AppNotification notification = new AppNotification();
		notification.setType(type);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setActionLink(actionLink);
		notification.setActor(actor);
		notification.setRead(false);
		notification.setTimestamp(java.time.LocalDateTime.now());
		return notification;
	}
}
