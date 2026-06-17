package com.system_gestion_soutenance.api.notification;

import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.common.service.MessageService;
import com.system_gestion_soutenance.api.notification.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Mock
	private MessageService messageService;

	@Test
	void sendEmail_mockMode_logsWithoutSending() {
		EmailService service = new EmailService(messageService);

		service.sendEmail("test@test.com", "Subject", "Body");
	}

	@Test
	void sendEmail_usesAutoConfiguredSender() {
		JavaMailSender autoSender = mock(JavaMailSender.class);
		when(autoSender.createMimeMessage()).thenReturn(mock(jakarta.mail.internet.MimeMessage.class));

		EmailService service = new EmailService(messageService);
		setField(service, "mailSender", autoSender);
		setField(service, "fromAddress", "test@example.com");

		service.sendEmail("test@test.com", "Subject", "Body");
		verify(autoSender).send(any(jakarta.mail.internet.MimeMessage.class));
	}

	@Test
	void sendVerificationEmail_callsSendEmail() {
		EmailService service = new EmailService(messageService);

		service.sendVerificationEmail("test@test.com", "John", "http://link");
	}

	@Test
	void sendPasswordResetEmail_callsSendEmail() {
		EmailService service = new EmailService(messageService);

		service.sendPasswordResetEmail("test@test.com", "http://link");
	}

	private void setField(EmailService service, String fieldName, Object value) {
		try {
			var field = EmailService.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(service, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
