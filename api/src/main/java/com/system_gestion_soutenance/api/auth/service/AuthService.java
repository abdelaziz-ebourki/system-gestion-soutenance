package com.system_gestion_soutenance.api.auth.service;

import com.system_gestion_soutenance.api.auth.dto.ForgotPasswordRequest;
import com.system_gestion_soutenance.api.auth.dto.LoginRequest;
import com.system_gestion_soutenance.api.auth.dto.LoginResponse;
import com.system_gestion_soutenance.api.auth.dto.ResetPasswordRequest;
import com.system_gestion_soutenance.api.auth.dto.VerifyRequest;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.auth.refresh.service.RefreshTokenService;
import com.system_gestion_soutenance.api.auth.refresh.service.RefreshTokenService.RotatedTokens;
import com.system_gestion_soutenance.api.auth.refresh.service.RefreshReuseException;
import com.system_gestion_soutenance.api.common.service.MessageService;
import com.system_gestion_soutenance.api.common.util.TokenHasher;
import com.system_gestion_soutenance.api.common.util.PasswordValidator;
import com.system_gestion_soutenance.api.common.util.ValidationResult;
import com.system_gestion_soutenance.api.notification.service.EmailService;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import com.system_gestion_soutenance.api.user.service.UserCacheService;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedAccessException;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
public class AuthService {

	private static final String DUMMY_BCRYPT_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final UserCacheService userCacheService;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;
	private final PasswordValidator passwordValidator;
	private final UserMapper userMapper;
	private final MessageService messageService;
	private final String baseUrl;

	public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider,
			RefreshTokenService refreshTokenService, UserCacheService userCacheService, PasswordEncoder passwordEncoder,
			EmailService emailService, PasswordValidator passwordValidator, UserMapper userMapper,
			MessageService messageService, @Value("${app.ui.base-url}") String baseUrl) {
		this.userRepository = userRepository;
		this.jwtTokenProvider = jwtTokenProvider;
		this.refreshTokenService = refreshTokenService;
		this.userCacheService = userCacheService;
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
		RotatedTokens rotated = refreshTokenService.create(user.getId());

		return new LoginResponse(userMapper.toDto(user), token, expiresAt, rotated.refreshToken(),
				System.currentTimeMillis() + refreshTokenService.getRefreshExpirationMs());
	}

	public LoginResponse refresh(String rawRefreshToken) {
		RotatedTokens rotated;
		try {
			rotated = refreshTokenService.rotate(rawRefreshToken);
		} catch (RefreshReuseException reuse) {
			refreshTokenService.revokeAll(reuse.getUserId());
			throw new UnauthorizedException("Session invalide ou expirée");
		}
		User user = userRepository.findById(rotated.userId())
				.orElseThrow(() -> new UnauthorizedException("Session invalide ou expirée"));
		if (!user.isActive()) {
			refreshTokenService.revokeAll(user.getId());
			throw new UnauthorizedException(messageService.getMessage("auth.login.account_inactive"));
		}
		String token = jwtTokenProvider.generateToken(String.valueOf(user.getId()), user.getRole().name());
		long expiresAt = System.currentTimeMillis() + jwtTokenProvider.getExpirationMs();
		return new LoginResponse(userMapper.toDto(user), token, expiresAt, rotated.refreshToken(),
				System.currentTimeMillis() + refreshTokenService.getRefreshExpirationMs());
	}

	public void logout(String rawRefreshToken) {
		if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
			refreshTokenService.revoke(rawRefreshToken);
		}
	}

	@Transactional
	public void verifyAccount(VerifyRequest request) {
		User user = userRepository.findByVerificationToken(TokenHasher.sha256Hex(request.token()))
				.orElseThrow(() -> new EntityNotFoundException(messageService.getMessage("auth.verify.invalid_token")));

		if (user.getVerificationTokenExpires() == null || Instant.now().isAfter(user.getVerificationTokenExpires())) {
			throw new InvalidBusinessStateException(messageService.getMessage("auth.verify.invalid_token"));
		}

		ValidationResult result = passwordValidator.validate(request.password());
		if (!result.valid()) {
			throw new InvalidBusinessStateException(result.errorMessage());
		}

		user.setPassword(passwordEncoder.encode(request.password()));
		user.setActive(true);
		user.setVerificationToken(null);
		user.setVerificationTokenExpires(null);
		userRepository.save(user);
		refreshTokenService.revokeAll(user.getId());
		userCacheService.evictUser(user.getId());
	}

	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {
		userRepository.findByEmail(request.email()).ifPresentOrElse(user -> {
			String rawToken = TokenHasher.generateSecureToken();
			user.setResetToken(TokenHasher.sha256Hex(rawToken));
			user.setResetTokenExpires(Instant.now().plusSeconds(3600));
			userRepository.save(user);
			String resetLink = baseUrl + "/reset-password?token=" + rawToken;
			emailService.sendPasswordResetEmail(request.email(), resetLink);
		}, () -> {
			passwordEncoder.matches("dummy-timing-burn", DUMMY_BCRYPT_HASH);
		});
	}

	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		User user = userRepository.findByResetToken(TokenHasher.sha256Hex(request.token())).orElseThrow(
				() -> new InvalidBusinessStateException(messageService.getMessage("auth.reset.invalid_token")));

		if (user.getResetTokenExpires() == null || Instant.now().isAfter(user.getResetTokenExpires())) {
			throw new InvalidBusinessStateException(messageService.getMessage("auth.reset.invalid_token"));
		}

		ValidationResult result = passwordValidator.validate(request.password());
		if (!result.valid()) {
			throw new InvalidBusinessStateException(result.errorMessage());
		}

		user.setPassword(passwordEncoder.encode(request.password()));
		user.setResetToken(null);
		user.setResetTokenExpires(null);
		userRepository.save(user);
		refreshTokenService.revokeAll(user.getId());
		userCacheService.evictUser(user.getId());
	}
}