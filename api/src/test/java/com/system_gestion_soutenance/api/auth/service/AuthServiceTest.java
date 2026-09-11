package com.system_gestion_soutenance.api.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.auth.dto.*;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.auth.refresh.service.RefreshTokenService;
import com.system_gestion_soutenance.api.auth.refresh.service.RefreshTokenService.RotatedTokens;
import com.system_gestion_soutenance.api.common.util.PasswordValidator;
import com.system_gestion_soutenance.api.common.util.TokenHasher;
import com.system_gestion_soutenance.api.common.util.ValidationResult;
import com.system_gestion_soutenance.api.notification.service.EmailService;
import com.system_gestion_soutenance.api.user.entity.Role;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedAccessException;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private EmailService emailService;

	@Mock
	private PasswordValidator passwordValidator;
	@Mock
	private com.system_gestion_soutenance.api.common.mapper.UserMapper userMapper;
	@Mock
	private com.system_gestion_soutenance.api.common.service.MessageService messageService;

	@Mock
	private RefreshTokenService refreshTokenService;

	@Mock
	private com.system_gestion_soutenance.api.user.service.UserCacheService userCacheService;

	private AuthService authService;

	@org.junit.jupiter.api.BeforeEach
	void setUp() {
		authService = new AuthService(userRepository, jwtTokenProvider, refreshTokenService, userCacheService,
				passwordEncoder, emailService, passwordValidator, userMapper, messageService, "http://localhost:5173");
	}

	private User createActiveUser() {
		User user = new User();
		user.setId(1L);
		user.setEmail("admin@test.com");
		user.setPassword("encoded-pass");
		user.setRole(Role.ADMIN);
		user.setActive(true);
		return user;
	}

	@Test
	void login_validCredentials_returnsLoginResponse() {
		User user = createActiveUser();
		when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password", "encoded-pass")).thenReturn(true);
		when(jwtTokenProvider.generateToken("1", "ADMIN")).thenReturn("jwt-token");
		when(jwtTokenProvider.getExpirationMs()).thenReturn(7200000L);
		when(refreshTokenService.create(1L)).thenReturn(new RotatedTokens(1L, "refresh-token"));
		when(refreshTokenService.getRefreshExpirationMs()).thenReturn(604800000L);
		when(userMapper.toDto(user)).thenReturn(new com.system_gestion_soutenance.api.user.dto.UserDto(user.getId(),
				user.getEmail(), user.getRole().name().toLowerCase(), user.getLastName(), user.getFirstName(),
				user.isActive(), null, null, null, null, null, null, null, null, null, null));

		LoginResponse response = authService.login(new LoginRequest("admin@test.com", "password"));

		assertNotNull(response);
		assertEquals("jwt-token", response.token());
		assertEquals("refresh-token", response.refreshToken());
		assertEquals("admin@test.com", response.user().email());
		assertTrue(response.expiresAt() > 0);
		assertTrue(response.refreshExpiresAt() > 0);
	}

	@Test
	void refresh_validToken_rotatesAndReturnsNewPair() {
		User user = createActiveUser();
		when(refreshTokenService.rotate("old-refresh")).thenReturn(new RotatedTokens(1L, "new-refresh"));
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(jwtTokenProvider.generateToken("1", "ADMIN")).thenReturn("new-jwt");
		when(jwtTokenProvider.getExpirationMs()).thenReturn(900000L);
		when(refreshTokenService.getRefreshExpirationMs()).thenReturn(604800000L);
		when(userMapper.toDto(user)).thenReturn(new com.system_gestion_soutenance.api.user.dto.UserDto(user.getId(),
				user.getEmail(), user.getRole().name().toLowerCase(), user.getLastName(), user.getFirstName(),
				user.isActive(), null, null, null, null, null, null, null, null, null, null));

		LoginResponse response = authService.refresh("old-refresh");

		assertEquals("new-jwt", response.token());
		assertEquals("new-refresh", response.refreshToken());
	}

	@Test
	void refresh_reusedToken_revokesFamilyAndThrows() {
		when(refreshTokenService.rotate("stolen-refresh"))
				.thenThrow(new com.system_gestion_soutenance.api.auth.refresh.service.RefreshReuseException(1L));

		assertThrows(UnauthorizedException.class, () -> authService.refresh("stolen-refresh"));
		verify(refreshTokenService).revokeAll(1L);
	}

	@Test
	void refresh_inactiveUser_revokesAllAndThrows() {
		User user = createActiveUser();
		user.setActive(false);
		when(refreshTokenService.rotate("old-refresh")).thenReturn(new RotatedTokens(1L, "new-refresh"));
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		assertThrows(UnauthorizedException.class, () -> authService.refresh("old-refresh"));
		verify(refreshTokenService).revokeAll(1L);
	}

	@Test
	void logout_withToken_revokesIt() {
		authService.logout("some-refresh");

		verify(refreshTokenService).revoke("some-refresh");
	}

	@Test
	void logout_nullToken_doesNothing() {
		authService.logout(null);

		verify(refreshTokenService, never()).revoke(any());
	}

	@Test
	void login_userNotFound_throwsUnauthorized() {
		when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

		assertThrows(UnauthorizedException.class,
				() -> authService.login(new LoginRequest("unknown@test.com", "password")));
	}

	@Test
	void login_wrongPassword_throwsUnauthorized() {
		User user = createActiveUser();
		when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong-pass", "encoded-pass")).thenReturn(false);

		assertThrows(UnauthorizedException.class,
				() -> authService.login(new LoginRequest("admin@test.com", "wrong-pass")));
	}

	@Test
	void login_inactiveUser_throwsForbidden() {
		User user = createActiveUser();
		user.setActive(false);
		when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password", "encoded-pass")).thenReturn(true);

		assertThrows(UnauthorizedAccessException.class,
				() -> authService.login(new LoginRequest("admin@test.com", "password")));
	}

	@Test
	void verifyAccount_validToken_activatesUser() {
		User user = createActiveUser();
		user.setActive(false);
		user.setVerificationToken(TokenHasher.sha256Hex("valid-token"));
		user.setVerificationTokenExpires(Instant.now().plusSeconds(3600));
		when(userRepository.findByVerificationToken(TokenHasher.sha256Hex("valid-token")))
				.thenReturn(Optional.of(user));
		when(passwordValidator.validate(anyString())).thenReturn(new ValidationResult(true, null));
		when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-pass");

		authService.verifyAccount(new VerifyRequest("valid-token", "new-password"));

		assertTrue(user.isActive());
		assertNull(user.getVerificationToken());
		assertNull(user.getVerificationTokenExpires());
		verify(userRepository).save(user);
		verify(refreshTokenService).revokeAll(1L);
		verify(userCacheService).evictUser(1L);
	}

	@Test
	void verifyAccount_expiredToken_throwsBadRequest() {
		User user = createActiveUser();
		user.setVerificationToken(TokenHasher.sha256Hex("stale-token"));
		user.setVerificationTokenExpires(Instant.now().minusSeconds(60));
		when(userRepository.findByVerificationToken(TokenHasher.sha256Hex("stale-token")))
				.thenReturn(Optional.of(user));

		assertThrows(InvalidBusinessStateException.class,
				() -> authService.verifyAccount(new VerifyRequest("stale-token", "password")));
	}

	@Test
	void verifyAccount_invalidToken_throwsNotFound() {
		when(userRepository.findByVerificationToken(TokenHasher.sha256Hex("bad-token"))).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class,
				() -> authService.verifyAccount(new VerifyRequest("bad-token", "password")));
	}

	@Test
	void forgotPassword_existingEmail_sendsEmail() {
		User user = createActiveUser();
		when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

		authService.forgotPassword(new ForgotPasswordRequest("admin@test.com"));

		assertNotNull(user.getResetToken());
		assertNotNull(user.getResetTokenExpires());
		verify(userRepository).save(user);
		verify(emailService).sendPasswordResetEmail(eq("admin@test.com"), anyString());
	}

	@Test
	void forgotPassword_nonexistentEmail_silentlyReturns() {
		when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

		authService.forgotPassword(new ForgotPasswordRequest("unknown@test.com"));

		verify(userRepository, never()).save(any());
		verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
	}

	@Test
	void resetPassword_validToken_resetsPassword() {
		User user = createActiveUser();
		user.setResetToken(TokenHasher.sha256Hex("reset-token"));
		user.setResetTokenExpires(Instant.now().plusSeconds(3600));
		when(userRepository.findByResetToken(TokenHasher.sha256Hex("reset-token"))).thenReturn(Optional.of(user));
		when(passwordValidator.validate(anyString())).thenReturn(new ValidationResult(true, null));
		when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-pass");

		authService.resetPassword(new ResetPasswordRequest("reset-token", "new-password"));

		assertNull(user.getResetToken());
		assertNull(user.getResetTokenExpires());
		verify(userRepository).save(user);
		verify(refreshTokenService).revokeAll(1L);
		verify(userCacheService).evictUser(1L);
	}

	@Test
	void resetPassword_expiredToken_throwsBadRequest() {
		User user = createActiveUser();
		user.setResetToken(TokenHasher.sha256Hex("expired-token"));
		user.setResetTokenExpires(Instant.now().minusSeconds(3600));
		when(userRepository.findByResetToken(TokenHasher.sha256Hex("expired-token"))).thenReturn(Optional.of(user));

		assertThrows(InvalidBusinessStateException.class,
				() -> authService.resetPassword(new ResetPasswordRequest("expired-token", "password")));
	}

	@Test
	void resetPassword_nullExpiry_throwsBadRequest() {
		User user = createActiveUser();
		user.setResetToken(TokenHasher.sha256Hex("no-expiry-token"));
		user.setResetTokenExpires(null);
		when(userRepository.findByResetToken(TokenHasher.sha256Hex("no-expiry-token"))).thenReturn(Optional.of(user));

		assertThrows(InvalidBusinessStateException.class,
				() -> authService.resetPassword(new ResetPasswordRequest("no-expiry-token", "password")));
	}

	@Test
	void resetPassword_invalidToken_throwsBadRequest() {
		when(userRepository.findByResetToken(TokenHasher.sha256Hex("bad-token"))).thenReturn(Optional.empty());

		assertThrows(InvalidBusinessStateException.class,
				() -> authService.resetPassword(new ResetPasswordRequest("bad-token", "password")));
	}
}
