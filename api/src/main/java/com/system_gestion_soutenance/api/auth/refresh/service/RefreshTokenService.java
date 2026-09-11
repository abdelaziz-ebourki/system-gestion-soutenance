package com.system_gestion_soutenance.api.auth.refresh.service;

import com.system_gestion_soutenance.api.auth.refresh.entity.RefreshToken;
import com.system_gestion_soutenance.api.auth.refresh.repository.RefreshTokenRepository;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedException;
import com.system_gestion_soutenance.api.common.util.TokenHasher;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
public class RefreshTokenService {

	private final RefreshTokenRepository repository;
	private final long refreshExpirationMs;

	public RefreshTokenService(RefreshTokenRepository repository,
			@Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
		this.repository = repository;
		this.refreshExpirationMs = refreshExpirationMs;
	}

	public long getRefreshExpirationMs() {
		return refreshExpirationMs;
	}

	@Transactional
	public RotatedTokens create(Long userId) {
		String rawToken = TokenHasher.generateSecureToken();
		RefreshToken token = new RefreshToken();
		token.setUserId(userId);
		token.setTokenHash(TokenHasher.sha256Hex(rawToken));
		token.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
		token.setRevoked(false);
		token.setCreatedAt(Instant.now());
		repository.save(token);
		return new RotatedTokens(userId, rawToken);
	}

	@Transactional
	public RotatedTokens rotate(String rawToken) {
		repository.deleteByExpiresAtBefore(Instant.now());
		String hash = TokenHasher.sha256Hex(rawToken);
		RefreshToken token = repository.findByTokenHash(hash)
				.orElseThrow(() -> new UnauthorizedException("Session invalide ou expirée"));

		if (token.isRevoked()) {
			throw new RefreshReuseException(token.getUserId());
		}
		if (token.getExpiresAt() == null || Instant.now().isAfter(token.getExpiresAt())) {
			repository.delete(token);
			throw new UnauthorizedException("Session invalide ou expirée");
		}

		String nextRawToken = TokenHasher.generateSecureToken();
		token.setRevoked(true);
		token.setReplacedBy(TokenHasher.sha256Hex(nextRawToken));
		repository.save(token);

		RefreshToken next = new RefreshToken();
		next.setUserId(token.getUserId());
		next.setTokenHash(TokenHasher.sha256Hex(nextRawToken));
		next.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
		next.setRevoked(false);
		next.setCreatedAt(Instant.now());
		repository.save(next);
		return new RotatedTokens(token.getUserId(), nextRawToken);
	}

	@Transactional
	public void revoke(String rawToken) {
		repository.findByTokenHash(TokenHasher.sha256Hex(rawToken)).ifPresent(repository::delete);
	}

	@Transactional
	public void revokeAll(Long userId) {
		repository.deleteByUserId(userId);
	}

	public record RotatedTokens(Long userId, String refreshToken) {
	}
}
