package com.system_gestion_soutenance.api.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

	private final PasswordValidator validator = new PasswordValidator();

	@Test
	void validateValidPassword() {
		// 8+ chars, upper, lower, digit, special
		assertDoesNotThrow(() -> validator.validate("Password123!"));
		assertDoesNotThrow(() -> validator.validate("A1b2C3d#"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"short1!", // Too short (< 8)
			"alllowercase1!", // Missing uppercase
			"ALLUPPERCASE1!", // Missing lowercase
			"NoSpecialChar1", // Missing special char
			"NoDigit!!@@", // Missing digit
			"    spaces1!", // Starts with space (regex \S+$ handles no whitespace)
			" Password123!" // Leading space
	})
	void validateInvalidPasswords(String password) {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> validator.validate(password));
		assertTrue(exception.getMessage().contains("Le mot de passe doit contenir"));
	}

	@Test
	void validateNullPassword() {
		assertThrows(IllegalArgumentException.class, () -> validator.validate(null));
	}
}
