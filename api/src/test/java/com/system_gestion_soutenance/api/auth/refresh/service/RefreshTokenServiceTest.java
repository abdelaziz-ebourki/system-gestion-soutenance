package com.system_gestion_soutenance.api.auth.refresh.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.system_gestion_soutenance.api.auth.refresh.entity.RefreshToken;
import com.system_gestion_soutenance.api.auth.refresh.repository.RefreshTokenRepository;
import com.system_gestion_soutenance.api.auth.refresh.service.RefreshTokenService.RotatedTokens;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedException;
import com.system_gestion_soutenance.api.common.util.TokenHasher;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

	@Mock
	private RefreshTokenRepository repository;

	private RefreshTokenService service;

	@BeforeEach
	void setUp() {
		service = new RefreshTokenService(repository, 604800000L);
	}

	private RefreshToken storedToken(String rawToken, Long userId) {
		RefreshToken token = new RefreshToken();
		token.setId(1L);
		token.setUserId(userId);
		token.setTokenHash(TokenHasher.sha256Hex(rawToken));
		token.setExpiresAt(Instant.now().plusSeconds(3600));
		token.setRevoked(false);
		token.setCreatedAt(Instant.now());
		return token;
	}

	@Test
	void create_persistsHashedToken() {
		RotatedTokens rotated = service.create(7L);

		assertNotNull(rotated.refreshToken());
		ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
		verify(repository).save(captor.capture());
		assertEquals(7L, captor.getValue().getUserId());
		assertEquals(TokenHasher.sha256Hex(rotated.refreshToken()), captor.getValue().getTokenHash());
		assertNotEquals(rotated.refreshToken(), captor.getValue().getTokenHash());
	}

	@Test
	void rotate_validToken_replacesIt() {
		RefreshToken stored = storedToken("old-raw", 7L);
		when(repository.findByTokenHash(TokenHasher.sha256Hex("old-raw"))).thenReturn(Optional.of(stored));

		RotatedTokens rotated = service.rotate("old-raw");

		assertEquals(7L, rotated.userId());
		assertNotEquals("old-raw", rotated.refreshToken());
		assertEquals(true, stored.isRevoked());
		assertNotNull(stored.getReplacedBy());
		verify(repository, org.mockito.Mockito.times(2)).save(any(RefreshToken.class));
	}

	@Test
	void rotate_unknownToken_throws401() {
		when(repository.findByTokenHash(TokenHasher.sha256Hex("ghost"))).thenReturn(Optional.empty());

		assertThrows(UnauthorizedException.class, () -> service.rotate("ghost"));
		verify(repository, never()).save(any());
	}

	@Test
	void rotate_revokedToken_revokesFamilyAndThrows401() {
		RefreshToken stored = storedToken("reused-raw", 7L);
		stored.setRevoked(true);
		when(repository.findByTokenHash(TokenHasher.sha256Hex("reused-raw"))).thenReturn(Optional.of(stored));

		assertThrows(UnauthorizedException.class, () -> service.rotate("reused-raw"));
		verify(repository).deleteByUserId(7L);
	}

	@Test
	void rotate_expiredToken_deletesAndThrows401() {
		RefreshToken stored = storedToken("stale-raw", 7L);
		stored.setExpiresAt(Instant.now().minusSeconds(10));
		when(repository.findByTokenHash(TokenHasher.sha256Hex("stale-raw"))).thenReturn(Optional.of(stored));

		assertThrows(UnauthorizedException.class, () -> service.rotate("stale-raw"));
		verify(repository).delete(stored);
	}

	@Test
	void revoke_existingToken_deletesIt() {
		RefreshToken stored = storedToken("bye-raw", 7L);
		when(repository.findByTokenHash(TokenHasher.sha256Hex("bye-raw"))).thenReturn(Optional.of(stored));

		service.revoke("bye-raw");

		verify(repository).delete(stored);
	}

	@Test
	void revoke_unknownToken_doesNothing() {
		when(repository.findByTokenHash(TokenHasher.sha256Hex("ghost"))).thenReturn(Optional.empty());

		service.revoke("ghost");

		verify(repository, never()).delete(any());
	}

	@Test
	void revokeAll_deletesUserTokens() {
		service.revokeAll(7L);

		verify(repository).deleteByUserId(7L);
	}
}
