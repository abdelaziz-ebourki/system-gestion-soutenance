package com.system_gestion_soutenance.api.common.exception;

import org.springframework.http.HttpStatus;
@SuppressWarnings("PMD")

public class UnauthorizedException extends BaseBusinessException {
	public UnauthorizedException(String message) {
		super(message, HttpStatus.UNAUTHORIZED);
	}
}