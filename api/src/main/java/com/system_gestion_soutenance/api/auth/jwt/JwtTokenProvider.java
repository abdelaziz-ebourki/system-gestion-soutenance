package com.system_gestion_soutenance.api.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	private final SecretKey key = Keys
			.hmacShaKeyFor("s3cr3t-k3y-f0r-d3f3ns3-m4n4g3m3nt-syst3m-2026".getBytes(StandardCharsets.UTF_8));

	private final long expirationMs = 2 * 60 * 60 * 1000L;

	public String generateToken(String userId, String role) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMs);

		return Jwts.builder().subject(userId).claim("role", role).issuedAt(now).expiration(expiry).signWith(key)
				.compact();
	}

	public String getUserIdFromToken(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public long getExpirationMs() {
		return expirationMs;
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
