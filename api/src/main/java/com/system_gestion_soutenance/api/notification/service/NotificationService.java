package com.system_gestion_soutenance.api.notification.service;

import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;

	public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, EmailService emailService) {
		this.notificationRepository = notificationRepository;
		this.userRepository = userRepository;
		this.emailService = emailService;
	}

	@Transactional(readOnly = true)
	public void sendNotificationEmail(Long notificationId) {
		AppNotification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification non trouvée"));

		List<User> users = userRepository.findAll();
		
		for (User user : users) {
			if (user.isActive()) {
				emailService.sendEmail(user.getEmail(), notification.getTitle(), notification.getMessage());
			}
		}
	}
}
