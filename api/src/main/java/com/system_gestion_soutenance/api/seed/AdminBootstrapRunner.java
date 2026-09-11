package com.system_gestion_soutenance.api.seed;

import com.system_gestion_soutenance.api.user.entity.Role;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
@SuppressWarnings("PMD")

@Component
@Profile("prod")
public class AdminBootstrapRunner implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(AdminBootstrapRunner.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final String adminEmail;
	private final String adminPassword;

	public AdminBootstrapRunner(UserRepository userRepository, PasswordEncoder passwordEncoder,
			@Value("${BOOTSTRAP_ADMIN_EMAIL:}") String adminEmail,
			@Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String adminPassword) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.adminEmail = adminEmail;
		this.adminPassword = adminPassword;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (userRepository.count() > 0) {
			return;
		}
		Assert.hasText(adminEmail,
				"No users found and BOOTSTRAP_ADMIN_EMAIL is not set — refusing to boot an admin-less instance");
		Assert.hasText(adminPassword,
				"No users found and BOOTSTRAP_ADMIN_PASSWORD is not set — refusing to boot an admin-less instance");

		User admin = new User();
		admin.setEmail(adminEmail);
		admin.setPassword(passwordEncoder.encode(adminPassword));
		admin.setRole(Role.ADMIN);
		admin.setActive(true);
		userRepository.save(admin);
		LOG.info("Bootstrapped initial admin account {}", adminEmail);
	}
}
