package com.system_gestion_soutenance.api.common.service;

import static org.junit.jupiter.api.Assertions.*;

import com.system_gestion_soutenance.api.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityServiceTest {

	private final SecurityService securityService = new SecurityService();

	@BeforeEach
	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getCurrentUserId_withAuthenticatedUser_returnsId() {
		User user = new User();
		user.setId(42L);
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, java.util.List.of()));

		Long result = securityService.getCurrentUserId();

		assertEquals(42L, result);
	}

	@Test
	void getCurrentUserId_withNoAuthentication_throwsException() {
		assertThrows(IllegalStateException.class, () -> securityService.getCurrentUserId());
	}

	@Test
	void getCurrentUserId_withAnonymousAuthentication_throwsException() {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("anonymousUser", "pass", java.util.List.of()));

		assertThrows(IllegalStateException.class, () -> securityService.getCurrentUserId());
	}

	@Test
	void getCurrentUser_withAuthenticatedUser_returnsUser() {
		User user = new User();
		user.setId(1L);
		user.setEmail("test@test.com");
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, java.util.List.of()));

		User result = securityService.getCurrentUser();

		assertEquals(1L, result.getId());
		assertEquals("test@test.com", result.getEmail());
	}

	@Test
	void getCurrentUser_withNoAuthentication_throwsException() {
		assertThrows(IllegalStateException.class, () -> securityService.getCurrentUser());
	}

	@Test
	void getCurrentUserEmail_withAuthenticatedUser_returnsEmail() {
		User user = new User();
		user.setEmail("user@test.com");
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, java.util.List.of()));

		assertEquals("user@test.com", securityService.getCurrentUserEmail());
	}

	@Test
	void getOptionalCurrentUserEmail_withAuthenticatedUser_returnsEmail() {
		User user = new User();
		user.setEmail("opt@test.com");
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, java.util.List.of()));

		assertEquals("opt@test.com", securityService.getOptionalCurrentUserEmail());
	}

	@Test
	void getOptionalCurrentUserEmail_withNoAuthentication_returnsNull() {
		assertNull(securityService.getOptionalCurrentUserEmail());
	}
}
