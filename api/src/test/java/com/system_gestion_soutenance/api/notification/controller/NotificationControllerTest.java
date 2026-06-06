package com.system_gestion_soutenance.api.notification.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.entity.NotificationType;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NotificationController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class NotificationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NotificationRepository repository;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private com.system_gestion_soutenance.api.notification.service.NotificationService notificationService;
	@MockitoBean
	private com.system_gestion_soutenance.api.common.mapper.AppNotificationMapper appNotificationMapper;

	@Test
	void findAll_returnsList() throws Exception {
		AppNotification n1 = new AppNotification(1L, NotificationType.INFO, "Test", "Message", LocalDateTime.now(),
				false, null, null);
		AppNotification n2 = new AppNotification(2L, NotificationType.WARNING, "Test 2", "Message 2",
				LocalDateTime.now(), true, null, null);
		when(repository.findAllByOrderByTimestampDesc()).thenReturn(List.of(n1, n2));
		when(appNotificationMapper.toDto(n1))
				.thenReturn(new com.system_gestion_soutenance.api.notification.dto.AppNotificationDto(1L, "info",
						"Test", "Message", n1.getTimestamp(), false, null, null));
		when(appNotificationMapper.toDto(n2))
				.thenReturn(new com.system_gestion_soutenance.api.notification.dto.AppNotificationDto(2L, "warning",
						"Test 2", "Message 2", n2.getTimestamp(), true, null, null));

		mockMvc.perform(get("/api/notifications")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].title").value("Test")).andExpect(jsonPath("$[1].title").value("Test 2"));
	}

	@Test
	void markRead_returns204() throws Exception {
		AppNotification notification = new AppNotification(1L, NotificationType.INFO, "Test", "Message",
				LocalDateTime.now(), false, null, null);
		when(repository.findById(1L)).thenReturn(Optional.of(notification));

		mockMvc.perform(patch("/api/notifications/1/read")).andExpect(status().isNoContent());

		verify(repository).save(argThat(n -> n.isRead()));
	}

	@Test
	void markRead_withUnknownId_returns204() throws Exception {
		when(repository.findById(99L)).thenReturn(Optional.empty());

		mockMvc.perform(patch("/api/notifications/99/read")).andExpect(status().isNoContent());

		verify(repository, never()).save(any());
	}

	@Test
	void markAllRead_returns204() throws Exception {
		AppNotification n1 = new AppNotification(1L, NotificationType.INFO, "A", "Msg", LocalDateTime.now(), false,
				null, null);
		AppNotification n2 = new AppNotification(2L, NotificationType.INFO, "B", "Msg", LocalDateTime.now(), false,
				null, null);
		when(repository.findAll()).thenReturn(List.of(n1, n2));

		mockMvc.perform(patch("/api/notifications/read-all")).andExpect(status().isNoContent());

		verify(repository).saveAll(argThat(list -> {
			var notifications = (List<AppNotification>) list;
			return notifications.stream().allMatch(AppNotification::isRead);
		}));
	}

	@Test
	void sendEmail_returns204() throws Exception {
		mockMvc.perform(post("/api/notifications/1/send-email")).andExpect(status().isNoContent());

		verify(notificationService).sendNotificationEmail(1L);
	}
}
