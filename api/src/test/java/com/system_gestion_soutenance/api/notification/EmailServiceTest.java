package com.system_gestion_soutenance.api.notification;

import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.email.entity.EmailConfig;
import com.system_gestion_soutenance.api.admin.config.email.repository.EmailConfigRepository;
import com.system_gestion_soutenance.api.common.service.MessageService;
import com.system_gestion_soutenance.api.common.util.EncryptionUtil;
import com.system_gestion_soutenance.api.notification.service.EmailService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Mock
	private EmailConfigRepository configRepository;
	@Mock
	private EncryptionUtil encryptionUtil;
	@Mock
	private MessageService messageService;

	@Test
	void sendEmail_mockMode_logsWithoutSending() {
		when(configRepository.findById(1L)).thenReturn(Optional.empty());
		JavaMailSender mailSender = mock(JavaMailSender.class);

		EmailService service = new EmailService(configRepository, encryptionUtil, messageService);

		service.sendEmail("test@test.com", "Subject", "Body");
		verifyNoInteractions(mailSender);
	}

	@Test
	void sendEmail_usesAutoConfiguredFallback_whenNoDbConfig() {
		when(configRepository.findById(1L)).thenReturn(Optional.empty());
		JavaMailSender autoSender = mock(JavaMailSender.class);
		when(autoSender.createMimeMessage()).thenReturn(mock(jakarta.mail.internet.MimeMessage.class));

		EmailService service = new EmailService(configRepository, encryptionUtil, messageService);
		setAutoConfiguredMailSender(service, autoSender);

		service.sendEmail("test@test.com", "Subject", "Body");
		verify(autoSender).send(any(jakarta.mail.internet.MimeMessage.class));
	}

	@Test
	void sendEmail_buildsSenderFromDbConfig_andDecryptsPassword() {
		EmailConfig config = new EmailConfig();
		config.setHost("smtp.example.com");
		config.setPort(587);
		config.setUsername("user");
		config.setPassword("encrypted-pass");
		config.setEncryption("tls");
		when(configRepository.findById(1L)).thenReturn(Optional.of(config));
		when(encryptionUtil.decrypt("encrypted-pass")).thenReturn("decrypted-pass");

		EmailService service = new EmailService(configRepository, encryptionUtil, messageService);

		service.sendEmail("test@test.com", "Subject", "Body");
		verify(encryptionUtil).decrypt("encrypted-pass");
	}

	@Test
	void sendEmail_buildsSenderFromDbConfig_withSsl() {
		EmailConfig config = new EmailConfig();
		config.setHost("smtp.example.com");
		config.setPort(465);
		config.setUsername("user");
		config.setPassword("encrypted-pass");
		config.setEncryption("SSL");
		when(configRepository.findById(1L)).thenReturn(Optional.of(config));
		when(encryptionUtil.decrypt("encrypted-pass")).thenReturn("decrypted-pass");

		EmailService service = new EmailService(configRepository, encryptionUtil, messageService);

		service.sendEmail("test@test.com", "Subject", "Body");
		verify(encryptionUtil).decrypt("encrypted-pass");
	}

	@Test
	void reconfigure_clearsCachedSender() {
		EmailConfig config = new EmailConfig();
		config.setHost("smtp.example.com");
		config.setPort(587);
		config.setPassword("encrypted-pass");

		org.mockito.Mockito.doReturn(Optional.of(config), Optional.empty()).when(configRepository).findById(1L);

		when(encryptionUtil.decrypt("encrypted-pass")).thenReturn("decrypted-pass");

		EmailService service = new EmailService(configRepository, encryptionUtil, messageService);

		service.sendEmail("test@test.com", "Subject", "Body");

		service.reconfigure();

		service.sendEmail("test@test.com", "Subject", "Body");
	}

	@Test
	void sendVerificationEmail_callsSendEmail() {
		when(configRepository.findById(1L)).thenReturn(Optional.empty());
		EmailService service = new EmailService(configRepository, encryptionUtil, messageService);

		service.sendVerificationEmail("test@test.com", "John", "http://link");
	}

	@Test
	void sendPasswordResetEmail_callsSendEmail() {
		when(configRepository.findById(1L)).thenReturn(Optional.empty());
		EmailService service = new EmailService(configRepository, encryptionUtil, messageService);

		service.sendPasswordResetEmail("test@test.com", "http://link");
	}

	private void setAutoConfiguredMailSender(EmailService service, JavaMailSender sender) {
		try {
			var field = EmailService.class.getDeclaredField("autoConfiguredMailSender");
			field.setAccessible(true);
			field.set(service, sender);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
