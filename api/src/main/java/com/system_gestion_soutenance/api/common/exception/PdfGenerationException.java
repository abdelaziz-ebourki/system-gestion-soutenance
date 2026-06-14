package com.system_gestion_soutenance.api.common.exception;

import org.springframework.http.HttpStatus;

public class PdfGenerationException extends BaseBusinessException {
	public PdfGenerationException(String message) {
		super(message, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public PdfGenerationException(String message, Throwable cause) {
		super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
