package com.system_gestion_soutenance.api.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.auth.dto.*;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.util.PasswordValidator;
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
import org.springframework.web.server.ResponseStatusException;

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
	private AuthService authService;

	@org.junit.jupiter.api.BeforeEach
	void setUp() {
		authService = new AuthService(userRepository, jwtTokenProvider, passwordEncoder, emailService,
				passwordValidator, userMapper, messageService, "http://localhost:5173");
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
		when(userMapper.toDto(user)).thenReturn(new com.system_gestion_soutenance.api.user.dto.UserDto(user.getId(),
				user.getEmail(), user.getRole().name().toLowerCase(), user.getLastName(), user.getFirstName(),
				user.isActive(), null, null, null, null, null, null, null, null, null));

		LoginResponse response = authService.login(new LoginRequest("admin@test.com", "password"));

		assertNotNull(response);
		assertEquals("jwt-token", response.token());
		assertEquals("admin@test.com", response.user().email());
		assertTrue(response.expiresAt() > 0);
	}

	@Test
	void login_userNotFound_throwsUnauthorized() {
		when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class,
				() -> authService.login(new LoginRequest("unknown@test.com", "password")));
	}

	@Test
	void login_wrongPassword_throwsUnauthorized() {
		User user = createActiveUser();
		when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong-pass", "encoded-pass")).thenReturn(false);

		assertThrows(ResponseStatusException.class,
				() -> authService.login(new LoginRequest("admin@test.com", "wrong-pass")));
	}

	@Test
	void login_inactiveUser_throwsForbidden() {
		User user = createActiveUser();
		user.setActive(false);
		when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password", "encoded-pass")).thenReturn(true);

		assertThrows(ResponseStatusException.class,
				() -> authService.login(new LoginRequest("admin@test.com", "password")));
	}

	@Test
	void verifyAccount_validToken_activatesUser() {
		User user = createActiveUser();
		user.setActive(false);
		user.setVerificationToken("valid-token");
		when(userRepository.findByVerificationToken("valid-token")).thenReturn(Optional.of(user));
		when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-pass");

		authService.verifyAccount(new VerifyRequest("valid-token", "new-password"));

		assertTrue(user.isActive());
		assertNull(user.getVerificationToken());
		verify(userRepository).save(user);
	}

	@Test
	void verifyAccount_invalidToken_throwsNotFound() {
		when(userRepository.findByVerificationToken("bad-token")).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class,
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
		user.setResetToken("reset-token");
		user.setResetTokenExpires(Instant.now().plusSeconds(3600));
		when(userRepository.findByResetToken("reset-token")).thenReturn(Optional.of(user));
		when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-pass");

		authService.resetPassword(new ResetPasswordRequest("reset-token", "new-password"));

		assertNull(user.getResetToken());
		assertNull(user.getResetTokenExpires());
		verify(userRepository).save(user);
	}

	@Test
	void resetPassword_expiredToken_throwsBadRequest() {
		User user = createActiveUser();
		user.setResetToken("expired-token");
		user.setResetTokenExpires(Instant.now().minusSeconds(3600));
		when(userRepository.findByResetToken("expired-token")).thenReturn(Optional.of(user));

		assertThrows(ResponseStatusException.class,
				() -> authService.resetPassword(new ResetPasswordRequest("expired-token", "password")));
	}

	@Test
	void resetPassword_nullExpiry_throwsBadRequest() {
		User user = createActiveUser();
		user.setResetToken("no-expiry-token");
		user.setResetTokenExpires(null);
		when(userRepository.findByResetToken("no-expiry-token")).thenReturn(Optional.of(user));

		assertThrows(ResponseStatusException.class,
				() -> authService.resetPassword(new ResetPasswordRequest("no-expiry-token", "password")));
	}

	@Test
	void resetPassword_invalidToken_throwsBadRequest() {
		when(userRepository.findByResetToken("bad-token")).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class,
				() -> authService.resetPassword(new ResetPasswordRequest("bad-token", "password")));
	}
}
