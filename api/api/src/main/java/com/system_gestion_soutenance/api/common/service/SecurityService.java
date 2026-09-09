package com.system_gestion_soutenance.api.common.service;

import com.system_gestion_soutenance.api.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
@SuppressWarnings("PMD")

@Service
public class SecurityService {

	public Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new IllegalStateException("No authenticated user found in security context");
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof User user) {
			return user.getId();
		}
		throw new IllegalStateException("Principal is not an instance of User");
	}

	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new IllegalStateException("No authenticated user found in security context");
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof User user) {
			return user;
		}
		throw new IllegalStateException("Principal is not an instance of User");
	}

	public String getCurrentUserEmail() {
		return getCurrentUser().getEmail();
	}

	public String getOptionalCurrentUserEmail() {
		try {
			return getCurrentUserEmail();
		} catch (IllegalStateException e) {
			return null;
		}
	}
}