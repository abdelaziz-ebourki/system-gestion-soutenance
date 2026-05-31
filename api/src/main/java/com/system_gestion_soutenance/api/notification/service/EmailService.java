package com.system_gestion_soutenance.api.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
	private static final String FROM = "noreply@soutenance-univ.ma";

	@Autowired(required = false)
	private JavaMailSender mailSender;

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
			helper.setFrom(FROM);
			mailSender.send(message);
			log.info("Email sent successfully to {}", to);
		} catch (MessagingException e) {
			log.error("Failed to send email to {}: {}", to, e.getMessage());
		}
	}

	public void sendVerificationEmail(String to, String firstName, String verificationLink) {
		String subject = "Activez votre compte";
		String body = """
				<h2>Bienvenue sur le syst\u00e8me de gestion de soutenances</h2>
				<p>Bonjour %s,</p>
				<p>Un administrateur a cr\u00e9\u00e9 votre compte. Cliquez sur le lien ci-dessous pour d\u00e9finir votre mot de passe et activer votre compte :</p>
				<p><a href="%s">%s</a></p>
				<p>Ce lien expire dans <strong>72 heures</strong>.</p>
				<hr>
				<p style="color: #666; font-size: 0.9em;">Syst\u00e8me de Gestion de Soutenances</p>
				"""
				.formatted(firstName, verificationLink, verificationLink);
		sendEmail(to, subject, body);
	}

	public void sendPasswordResetEmail(String to, String resetLink) {
		String subject = "R\u00e9initialisation de mot de passe";
		String body = """
				<h2>R\u00e9initialisation de mot de passe</h2>
				<p>Bonjour,</p>
				<p>Vous avez demand\u00e9 la r\u00e9initialisation de votre mot de passe.</p>
				<p>Cliquez sur le lien ci-dessous pour d\u00e9finir un nouveau mot de passe :</p>
				<p><a href="%s">%s</a></p>
				<p>Ce lien expire dans <strong>1 heure</strong>.</p>
				<p>Si vous n'\u00eates pas \u00e0 l'origine de cette demande, ignorez cet email.</p>
				<hr>
				<p style="color: #666; font-size: 0.9em;">Systeme de Gestion de Soutenances</p>
				""".formatted(resetLink, resetLink);
		sendEmail(to, subject, body);
	}
}
