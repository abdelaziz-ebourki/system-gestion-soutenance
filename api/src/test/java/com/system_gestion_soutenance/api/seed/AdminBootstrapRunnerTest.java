package com.system_gestion_soutenance.api.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.system_gestion_soutenance.api.user.entity.Role;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Test
	void emptyDbWithEnv_createsActiveAdmin() {
		when(userRepository.count()).thenReturn(0L);
		when(passwordEncoder.encode("s3cret")).thenReturn("encoded");
		AdminBootstrapRunner runner = new AdminBootstrapRunner(userRepository, passwordEncoder, "root@example.com",
				"s3cret");

		runner.run();

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		assertEquals("root@example.com", captor.getValue().getEmail());
		assertEquals("encoded", captor.getValue().getPassword());
		assertEquals(Role.ADMIN, captor.getValue().getRole());
		assertTrue(captor.getValue().isActive());
	}

	@Test
	void nonEmptyDb_skipsSilently() {
		when(userRepository.count()).thenReturn(5L);
		AdminBootstrapRunner runner = new AdminBootstrapRunner(userRepository, passwordEncoder, "", "");

		runner.run();

		verify(userRepository, never()).save(any());
	}

	@Test
	void emptyDbWithoutEnv_failsFast() {
		when(userRepository.count()).thenReturn(0L);
		AdminBootstrapRunner runner = new AdminBootstrapRunner(userRepository, passwordEncoder, "", "");

		assertThrows(IllegalArgumentException.class, runner::run);
		verify(userRepository, never()).save(any());
	}
}
