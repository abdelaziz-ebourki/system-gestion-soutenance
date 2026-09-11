package com.system_gestion_soutenance.api.auth.refresh.repository;

import com.system_gestion_soutenance.api.auth.refresh.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	void deleteByUserId(Long userId);

	@Modifying
	void deleteByExpiresAtBefore(Instant now);
}
