package com.system_gestion_soutenance.api.auth.service;

import com.system_gestion_soutenance.api.auth.dto.ForgotPasswordRequest;
import com.system_gestion_soutenance.api.auth.dto.LoginRequest;
import com.system_gestion_soutenance.api.auth.dto.LoginResponse;
import com.system_gestion_soutenance.api.auth.dto.ResetPasswordRequest;
import com.system_gestion_soutenance.api.auth.dto.VerifyRequest;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.notification.service.EmailService;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;

	public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider,
			PasswordEncoder passwordEncoder, EmailService emailService) {
		this.userRepository = userRepository;
		this.jwtTokenProvider = jwtTokenProvider;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
	}

	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
						"Identifiants invalides (E-mail ou mot de passe incorrect)"));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"Identifiants invalides (E-mail ou mot de passe incorrect)");
		}

		if (!user.isActive()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"Veuillez activer votre compte via le lien de vérification envoyé à votre adresse email.");
		}

		String token = jwtTokenProvider.generateToken(String.valueOf(user.getId()), user.getRole().name());
		long expiresAt = System.currentTimeMillis() + jwtTokenProvider.getExpirationMs();

		return new LoginResponse(UserDto.from(user), token, expiresAt);
	}

	@Transactional
	public void verifyAccount(VerifyRequest request) {
		User user = userRepository.findByVerificationToken(request.token())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Token de vérification invalide ou déjà utilisé"));

		user.setPassword(passwordEncoder.encode(request.password()));
		user.setActive(true);
		user.setVerificationToken(null);
		userRepository.save(user);
	}

	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {
		userRepository.findByEmail(request.email()).ifPresent(user -> {
			user.setResetToken(UUID.randomUUID().toString());
			user.setResetTokenExpires(Instant.now().plusSeconds(3600));
			userRepository.save(user);
			String resetLink = "/reset-password?token=" + user.getResetToken();
			emailService.sendPasswordResetEmail(request.email(), resetLink);
		});
	}

	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		User user = userRepository.findByResetToken(request.token())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Token de réinitialisation invalide ou expiré"));

		if (user.getResetTokenExpires() == null || Instant.now().isAfter(user.getResetTokenExpires())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de réinitialisation invalide ou expiré");
		}

		user.setPassword(passwordEncoder.encode(request.password()));
		user.setResetToken(null);
		user.setResetTokenExpires(null);
		userRepository.save(user);
	}
}
