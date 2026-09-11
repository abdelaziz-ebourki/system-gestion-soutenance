package com.system_gestion_soutenance.api.auth.refresh.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.system_gestion_soutenance.api.auth.refresh.entity.RefreshToken;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class RefreshTokenRepositoryTest {

	@Autowired
	private RefreshTokenRepository repository;

	private RefreshToken token(Long userId, String hash) {
		RefreshToken token = new RefreshToken();
		token.setUserId(userId);
		token.setTokenHash(hash);
		token.setExpiresAt(Instant.now().plusSeconds(3600));
		token.setRevoked(false);
		token.setCreatedAt(Instant.now());
		return token;
	}

	@Test
	void deleteByUserId_removesOnlyThatUsersTokens() {
		repository.save(token(1L, "hash-a"));
		repository.save(token(1L, "hash-b"));
		repository.save(token(2L, "hash-c"));

		repository.deleteByUserId(1L);

		assertTrue(repository.findByTokenHash("hash-a").isEmpty());
		assertTrue(repository.findByTokenHash("hash-b").isEmpty());
		assertTrue(repository.findByTokenHash("hash-c").isPresent());
	}

	@Test
	void deleteByExpiresAtBefore_purgesOnlyStaleTokens() {
		RefreshToken stale = token(1L, "hash-old");
		stale.setExpiresAt(Instant.now().minusSeconds(10));
		repository.save(stale);
		repository.save(token(1L, "hash-fresh"));

		repository.deleteByExpiresAtBefore(Instant.now());

		assertEquals(1, repository.count());
		assertTrue(repository.findByTokenHash("hash-fresh").isPresent());
	}
}
