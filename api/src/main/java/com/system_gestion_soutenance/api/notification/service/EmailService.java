package com.system_gestion_soutenance.api.notification.service;

import com.system_gestion_soutenance.api.admin.config.email.entity.EmailConfig;
import com.system_gestion_soutenance.api.admin.config.email.repository.EmailConfigRepository;
import com.system_gestion_soutenance.api.common.util.EncryptionUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.system_gestion_soutenance.api.common.service.MessageService;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
	private static final String FROM = "noreply@soutenance-univ.ma";

	private final EmailConfigRepository configRepository;
	private final EncryptionUtil encryptionUtil;
	private final MessageService messageService;

	@Autowired(required = false)
	private JavaMailSender autoConfiguredMailSender;

	private volatile JavaMailSender currentMailSender;

	public EmailService(EmailConfigRepository configRepository, EncryptionUtil encryptionUtil,
			MessageService messageService) {
		this.configRepository = configRepository;
		this.encryptionUtil = encryptionUtil;
		this.messageService = messageService;
	}

	public void reconfigure() {
		synchronized (this) {
			currentMailSender = null;
		}
	}

	private JavaMailSender getMailSender() {
		JavaMailSender sender = currentMailSender;
		if (sender != null) {
			return sender;
		}
		return buildMailSender();
	}

	private synchronized JavaMailSender buildMailSender() {
		if (currentMailSender != null) {
			return currentMailSender;
		}

		EmailConfig config = configRepository.findById(1L).orElse(null);
		if (config != null && config.getHost() != null && !config.getHost().isBlank()) {
			currentMailSender = createSender(config);
			return currentMailSender;
		}

		if (autoConfiguredMailSender != null) {
			return autoConfiguredMailSender;
		}

		return null;
	}

	private JavaMailSender createSender(EmailConfig config) {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(config.getHost());
		sender.setPort(config.getPort());
		sender.setUsername(config.getUsername());
		if (config.getPassword() != null) {
			sender.setPassword(encryptionUtil.decrypt(config.getPassword()));
		}

		Properties props = sender.getJavaMailProperties();
		String enc = config.getEncryption();
		if ("tls".equalsIgnoreCase(enc)) {
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
		} else if ("ssl".equalsIgnoreCase(enc)) {
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.ssl.enable", "true");
			props.put("mail.smtp.socketFactory.port", String.valueOf(config.getPort()));
		} else {
			props.put("mail.smtp.auth", "true");
		}

		return sender;
	}

	@Async
	public void sendEmail(String to, String subject, String body) {
		JavaMailSender sender = getMailSender();
		if (sender == null) {
			log.info("[Mock Email] To: {} | Subject: {} | Body: {}", to, subject, body);
			return;
		}
		try {
			MimeMessage message = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(body, true);
			helper.setFrom(FROM);
			sender.send(message);
			log.info("Email sent successfully to {}", to);
		} catch (MessagingException | MailException e) {
			log.error("Failed to send email to {}: {}", to, e.getMessage());
		}
	}

	@Async
	public void sendVerificationEmail(String to, String firstName, String verificationLink) {
		String subject = messageService.getMessage("email.verify.subject");
		String body = """
				<h2>%s</h2>
				<p>%s</p>
				<p>%s</p>
				<p><a href="%s">%s</a></p>
				<p>%s</p>
				<hr>
				<p style="color: #666; font-size: 0.9em;">Syst\u00e8me de Gestion de Soutenances</p>
				""".formatted(messageService.getMessage("email.verify.title"),
				messageService.getMessage("email.generic.greeting", firstName),
				messageService.getMessage("email.verify.body"), verificationLink, verificationLink,
				messageService.getMessage("email.verify.expiry"));
		sendEmail(to, subject, body);
	}

	@Async
	public void sendPasswordResetEmail(String to, String resetLink) {
		String subject = messageService.getMessage("email.reset.subject");
		String body = """
				<h2>%s</h2>
				<p>Bonjour,</p>
				<p>%s</p>
				<p>%s</p>
				<p><a href="%s">%s</a></p>
				<p>%s</p>
				<p>%s</p>
				<hr>
				<p style="color: #666; font-size: 0.9em;">Systeme de Gestion de Soutenances</p>
				""".formatted(messageService.getMessage("email.reset.subject"),
				messageService.getMessage("email.reset.body1"), messageService.getMessage("email.reset.body2"),
				resetLink, resetLink, messageService.getMessage("email.reset.expiry"),
				messageService.getMessage("email.reset.ignore"));
		sendEmail(to, subject, body);
	}
}
