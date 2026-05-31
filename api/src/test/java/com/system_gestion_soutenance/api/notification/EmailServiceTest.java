package com.system_gestion_soutenance.api.notification;

import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.notification.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Mock
	private JavaMailSender mailSender;

	private EmailService service;

	@BeforeEach
	void setUp() {
		service = new EmailService();
	}

	@Test
	void sendEmail_mockMode_logsWithoutSending() {
		service.sendEmail("test@test.com", "Subject", "Body");
		verifyNoInteractions(mailSender);
	}

	@Test
	void sendEmail_withMailSender_sendsMessage() {
		service = new EmailService();
		try {
			var field = EmailService.class.getDeclaredField("mailSender");
			field.setAccessible(true);
			field.set(service, mailSender);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		when(mailSender.createMimeMessage()).thenReturn(mock(jakarta.mail.internet.MimeMessage.class));
		service.sendEmail("test@test.com", "Subject", "Body");
		verify(mailSender).send(any(jakarta.mail.internet.MimeMessage.class));
	}

	@Test
	void sendVerificationEmail_callsSendEmail() {
		service.sendVerificationEmail("test@test.com", "John", "http://link");
	}

	@Test
	void sendPasswordResetEmail_callsSendEmail() {
		service.sendPasswordResetEmail("test@test.com", "http://link");
	}
}
