package com.system_gestion_soutenance.api.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
@SuppressWarnings("PMD")

@Component
public class JwtTokenProvider {

	private final SecretKey key;
	private final long accessExpirationMs;

	public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.access-expiration-ms:900000}") long accessExpirationMs) {
		Assert.hasText(secret, "app.jwt.secret must be set (env JWT_SECRET) with at least 32 bytes");
		Assert.isTrue(secret.getBytes(StandardCharsets.UTF_8).length >= 32,
				"app.jwt.secret must be at least 32 bytes for HS256");
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessExpirationMs = accessExpirationMs;
	}

	public String generateToken(String userId, String role) {

		Date now = new Date();
		Date expiry = new Date(now.getTime() + accessExpirationMs);

		return Jwts.builder().subject(userId).claim("role", role).issuedAt(now).expiration(expiry).signWith(key)
				.compact();
	}

	public String getUserIdFromToken(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public long getExpirationMs() {
		return accessExpirationMs;
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
