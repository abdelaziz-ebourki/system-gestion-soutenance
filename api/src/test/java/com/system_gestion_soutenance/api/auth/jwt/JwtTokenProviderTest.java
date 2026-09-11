package com.system_gestion_soutenance.api.auth.jwt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

	private JwtTokenProvider tokenProvider;

	@BeforeEach
	void setUp() {
		tokenProvider = new JwtTokenProvider("s3cr3t-k3y-f0r-d3f3ns3-m4n4g3m3nt-syst3m-2026", 900000L);
	}

	@Test
	void generateToken_shouldReturnValidJwt() {
		String token = tokenProvider.generateToken("1", "ADMIN");
		assertNotNull(token);
		assertTrue(token.split("\\.").length == 3);
	}

	@Test
	void getUserIdFromToken_shouldExtractSubject() {
		String token = tokenProvider.generateToken("42", "TEACHER");
		String userId = tokenProvider.getUserIdFromToken(token);
		assertEquals("42", userId);
	}

	@Test
	void getExpirationMs_shouldReturnConfiguredLifetime() {
		assertEquals(900000L, tokenProvider.getExpirationMs());
	}

	@Test
	void constructor_blankSecret_shouldFailFast() {
		assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider("   ", 900000L));
	}

	@Test
	void constructor_shortSecret_shouldFailFast() {
		assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider("too-short", 900000L));
	}

	@Test
	void validateToken_withValidToken_shouldReturnTrue() {
		String token = tokenProvider.generateToken("1", "ADMIN");
		assertTrue(tokenProvider.validateToken(token));
	}

	@Test
	void validateToken_withInvalidToken_shouldReturnFalse() {
		assertFalse(tokenProvider.validateToken("invalid-token-string"));
	}

	@Test
	void validateToken_withEmptyToken_shouldReturnFalse() {
		assertFalse(tokenProvider.validateToken(""));
	}
}
