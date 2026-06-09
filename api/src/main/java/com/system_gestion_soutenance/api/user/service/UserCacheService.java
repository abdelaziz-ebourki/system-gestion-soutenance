package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.Optional;
@SuppressWarnings("PMD")

@Service
public class UserCacheService {

	private final UserRepository userRepository;

	public UserCacheService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Cacheable(value = "users", key = "#id")
	public Optional<User> getUserById(Long id) {
		return userRepository.findById(id);
	}
}