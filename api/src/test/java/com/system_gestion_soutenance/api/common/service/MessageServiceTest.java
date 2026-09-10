package com.system_gestion_soutenance.api.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@Mock
	private MessageSource messageSource;

	@InjectMocks
	private MessageService service;

	@Test
	void getMessage_returnsLocalizedMessage() {
		when(messageSource.getMessage(eq("test.code"), any(Object[].class), any(Locale.class)))
				.thenReturn("Résultat attendu");

		String result = service.getMessage("test.code", "arg1");

		assertEquals("Résultat attendu", result);
	}

	@Test
	void getMessage_whenCodeNotFound_throwsException() {
		when(messageSource.getMessage(eq("unknown.code"), any(Object[].class), any(Locale.class)))
				.thenThrow(new NoSuchMessageException("unknown.code"));

		assertThrows(NoSuchMessageException.class, () -> service.getMessage("unknown.code"));
	}
}
