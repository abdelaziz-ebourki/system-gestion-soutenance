package com.system_gestion_soutenance.api.notification.service;

import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
@SuppressWarnings("PMD")

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;

	public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
			EmailService emailService) {
		this.notificationRepository = notificationRepository;
		this.userRepository = userRepository;
		this.emailService = emailService;
	}

	@Transactional(readOnly = true)
	public PaginatedResponse<AppNotification> findAll(int page, int limit) {
		Page<AppNotification> notifPage = notificationRepository
				.findAllByOrderByTimestampDesc(PageRequest.of(page, limit));
		return new PaginatedResponse<>(notifPage.getContent(), notifPage.getTotalElements(), notifPage.getTotalPages(),
				page, limit);
	}

	@Transactional(readOnly = true)
	public PaginatedResponse<AppNotification> findAllByUser(Long userId, int page, int limit) {
		Page<AppNotification> notifPage = notificationRepository.findByUserIdOrUserIdIsNullOrderByTimestampDesc(userId,
				PageRequest.of(page, limit));
		return new PaginatedResponse<>(notifPage.getContent(), notifPage.getTotalElements(), notifPage.getTotalPages(),
				page, limit);
	}

	@Transactional(readOnly = true)
	public void sendNotificationEmail(Long notificationId) {
		AppNotification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new EntityNotFoundException("Notification non trouvée"));

		List<User> users = userRepository.findAll();

		for (User user : users) {
			if (user.isActive()) {
				emailService.sendEmail(user.getEmail(), notification.getTitle(), notification.getMessage());
			}
		}
	}
}