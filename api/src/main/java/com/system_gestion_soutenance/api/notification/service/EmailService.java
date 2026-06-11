package com.system_gestion_soutenance.api.notification.service;

import com.system_gestion_soutenance.api.common.service.MessageService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final MessageService messageService;

	@Autowired(required = false)
	private JavaMailSender mailSender;

	@Value("${app.mail.from:noreply@soutenance-univ.ma}")
	private String fromAddress;

	public EmailService(MessageService messageService) {
		this.messageService = messageService;
	}

	@Async
	public void sendEmail(String to, String subject, String body) {
		if (mailSender == null) {
			log.info("[Mock Email] To: {} | Subject: {} | Body: {}", to, subject, body);
			return;
		}
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(body, true);
			helper.setFrom(fromAddress);
			mailSender.send(message);
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
