package com.system_gestion_soutenance.api.common.util;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;
@SuppressWarnings("PMD")

@Component
public class PasswordValidator {

	// Minimum 8 characters, at least one uppercase letter, one lowercase letter,
	// one number and one special character
	private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
	private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);

	public void validate(String password) {
		if (password == null || !PATTERN.matcher(password).matches()) {
			throw new IllegalArgumentException(
					"Le mot de passe doit contenir au moins 8 caractères, inclure une majuscule, "
							+ "une minuscule, un chiffre et un caractère spécial.");
		}
	}
}