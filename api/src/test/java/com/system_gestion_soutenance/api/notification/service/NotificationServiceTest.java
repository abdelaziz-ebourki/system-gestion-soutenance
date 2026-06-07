package com.system_gestion_soutenance.api.notification.service;

import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.entity.NotificationType;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private EmailService emailService;

	@InjectMocks
	private NotificationService notificationService;

	private AppNotification notification;
	private User activeUser;
	private User inactiveUser;

	@BeforeEach
	void setUp() {
		notification = new AppNotification(1L, NotificationType.INFO, "Title", "Message", LocalDateTime.now(), false,
				null, null);

		activeUser = new User();
		activeUser.setEmail("active@test.com");
		activeUser.setActive(true);

		inactiveUser = new User();
		inactiveUser.setEmail("inactive@test.com");
		inactiveUser.setActive(false);
	}

	@Test
	void sendNotificationEmail_Success() {
		when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
		when(userRepository.findAll()).thenReturn(List.of(activeUser, inactiveUser));

		notificationService.sendNotificationEmail(1L);

		verify(emailService, times(1)).sendEmail("active@test.com", "Title", "Message");
		verify(emailService, never()).sendEmail(eq("inactive@test.com"), anyString(), anyString());
	}

	@Test
	void sendNotificationEmail_NotFound() {
		when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
			notificationService.sendNotificationEmail(1L);
		});

		assertEquals("Notification non trouvée", exception.getMessage());
	}
}
