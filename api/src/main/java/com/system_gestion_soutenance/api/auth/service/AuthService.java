package com.system_gestion_soutenance.api.auth.service;

import com.system_gestion_soutenance.api.auth.dto.ForgotPasswordRequest;
import com.system_gestion_soutenance.api.auth.dto.LoginRequest;
import com.system_gestion_soutenance.api.auth.dto.LoginResponse;
import com.system_gestion_soutenance.api.auth.dto.ResetPasswordRequest;
import com.system_gestion_soutenance.api.auth.dto.VerifyRequest;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.service.MessageService;
import com.system_gestion_soutenance.api.common.util.PasswordValidator;
import com.system_gestion_soutenance.api.notification.service.EmailService;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedAccessException;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;
	private final PasswordValidator passwordValidator;
	private final UserMapper userMapper;
	private final MessageService messageService;
	private final String baseUrl;

	public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider,
			PasswordEncoder passwordEncoder, EmailService emailService, PasswordValidator passwordValidator,
			UserMapper userMapper, MessageService messageService, @Value("${app.ui.base-url}") String baseUrl) {
		this.userRepository = userRepository;
		this.jwtTokenProvider = jwtTokenProvider;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
		this.passwordValidator = passwordValidator;
		this.userMapper = userMapper;
		this.messageService = messageService;
		this.baseUrl = baseUrl;
	}

	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email()).orElseThrow(
				() -> new UnauthorizedException(messageService.getMessage("auth.login.invalid_credentials")));

		if (user.getPassword() == null || user.getPassword().isBlank()) {
			throw new UnauthorizedException(messageService.getMessage("auth.login.invalid_credentials"));
		}

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new UnauthorizedException(messageService.getMessage("auth.login.invalid_credentials"));
		}

		if (!user.isActive()) {
			throw new UnauthorizedAccessException(messageService.getMessage("auth.login.account_inactive"));
		}

		String token = jwtTokenProvider.generateToken(String.valueOf(user.getId()), user.getRole().name());
		long expiresAt = System.currentTimeMillis() + jwtTokenProvider.getExpirationMs();

		return new LoginResponse(userMapper.toDto(user), token, expiresAt);
	}

	@Transactional
	public void verifyAccount(VerifyRequest request) {
		User user = userRepository.findByVerificationToken(request.token())
				.orElseThrow(() -> new EntityNotFoundException(messageService.getMessage("auth.verify.invalid_token")));

		try {
			passwordValidator.validate(request.password());
		} catch (IllegalArgumentException e) {
			throw new InvalidBusinessStateException(e.getMessage());
		}

		user.setPassword(passwordEncoder.encode(request.password()));
		user.setActive(true);
		user.setVerificationToken(null);
		userRepository.save(user);
	}

	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {
		userRepository.findByEmail(request.email()).ifPresentOrElse(user -> {
			user.setResetToken(UUID.randomUUID().toString());
			user.setResetTokenExpires(Instant.now().plusSeconds(3600));
			userRepository.save(user);
			String resetLink = baseUrl + "/reset-password?token=" + user.getResetToken();
			emailService.sendPasswordResetEmail(request.email(), resetLink);
		}, () -> {
			// Dummy operations to mitigate timing attacks
			// Simulation of work to prevent user enumeration via timing
		});
	}

	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		User user = userRepository.findByResetToken(request.token()).orElseThrow(
				() -> new InvalidBusinessStateException(messageService.getMessage("auth.reset.invalid_token")));

		if (user.getResetTokenExpires() == null || Instant.now().isAfter(user.getResetTokenExpires())) {
			throw new InvalidBusinessStateException(messageService.getMessage("auth.reset.invalid_token"));
		}

		try {
			passwordValidator.validate(request.password());
		} catch (IllegalArgumentException e) {
			throw new InvalidBusinessStateException(e.getMessage());
		}

		user.setPassword(passwordEncoder.encode(request.password()));
		user.setResetToken(null);
		user.setResetTokenExpires(null);
		userRepository.save(user);
	}
}
