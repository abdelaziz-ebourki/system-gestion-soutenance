package com.system_gestion_soutenance.api.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordValidatorTest {

	private final PasswordValidator validator = new PasswordValidator();

	@Test
	void validPassword_doesNotThrow() {
		assertDoesNotThrow(() -> validator.validate("Abcdef1@"));
	}

	@Test
	void validPassword_longer_doesNotThrow() {
		assertDoesNotThrow(() -> validator.validate("LongerPassword1$"));
	}

	@Test
	void nullPassword_throws() {
		assertThrows(IllegalArgumentException.class, () -> validator.validate(null));
	}

	@Test
	void missingDigit_throws() {
		assertThrows(IllegalArgumentException.class, () -> validator.validate("Abcdefg@"));
	}

	@Test
	void missingLowercase_throws() {
		assertThrows(IllegalArgumentException.class, () -> validator.validate("ABCDEF1@"));
	}

	@Test
	void missingUppercase_throws() {
		assertThrows(IllegalArgumentException.class, () -> validator.validate("abcdef1@"));
	}

	@Test
	void missingSpecialChar_throws() {
		assertThrows(IllegalArgumentException.class, () -> validator.validate("Abcdef12"));
	}

	@Test
	void containsWhitespace_throws() {
		assertThrows(IllegalArgumentException.class, () -> validator.validate("Abc def1@"));
	}

	@Test
	void tooShort_throws() {
		assertThrows(IllegalArgumentException.class, () -> validator.validate("Ab1@"));
	}
}
