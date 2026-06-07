package com.system_gestion_soutenance.api.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends BaseBusinessException {
	public UnauthorizedAccessException(String message) {
		super(message, HttpStatus.FORBIDDEN);
	}
}
