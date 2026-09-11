package com.system_gestion_soutenance.api.auth.refresh.service;

@SuppressWarnings("PMD")

public class RefreshReuseException extends RuntimeException {

	private final Long userId;

	public RefreshReuseException(Long userId) {
		super("Refresh token reuse detected");
		this.userId = userId;
	}

	public Long getUserId() {
		return userId;
	}
}
