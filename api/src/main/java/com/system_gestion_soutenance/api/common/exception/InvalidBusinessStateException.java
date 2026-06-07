package com.system_gestion_soutenance.api.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidBusinessStateException extends BaseBusinessException {
	public InvalidBusinessStateException(String message) {
		super(message, HttpStatus.BAD_REQUEST);
	}
}
