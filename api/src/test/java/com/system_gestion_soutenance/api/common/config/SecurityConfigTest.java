package com.system_gestion_soutenance.api.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

	private final PasswordEncoder passwordEncoder = new SecurityConfig(null, null).passwordEncoder();

	@Test
	void passwordEncoder_isBCrypt() {
		assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
	}

	@Test
	void passwordEncoder_encodesAndMatches() {
		String raw = "myPassword123";
		String hash = passwordEncoder.encode(raw);
		assertThat(hash).isNotEqualTo(raw);
		assertThat(passwordEncoder.matches(raw, hash)).isTrue();
		assertThat(passwordEncoder.matches("wrong", hash)).isFalse();
	}
}
