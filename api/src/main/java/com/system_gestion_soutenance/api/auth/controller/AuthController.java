package com.system_gestion_soutenance.api.auth.controller;

import com.system_gestion_soutenance.api.auth.dto.ForgotPasswordRequest;
import com.system_gestion_soutenance.api.auth.dto.LoginRequest;
import com.system_gestion_soutenance.api.auth.dto.LoginResponse;
import com.system_gestion_soutenance.api.auth.dto.ResetPasswordRequest;
import com.system_gestion_soutenance.api.auth.dto.VerifyRequest;
import com.system_gestion_soutenance.api.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	@Operation(summary = "Authenticate a user", description = "Validates credentials and returns a JWT token with user info.")
	@ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
	@ApiResponse(responseCode = "401", description = "Invalid email or password", content = @Content(examples = @ExampleObject("{\"message\": \"Identifiants invalides (E-mail ou mot de passe incorrect)\"}")))
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
		LoginResponse loginResponse = authService.login(request);

		String cookieValue = String.format("jwt_token=%s; Max-Age=%d; Path=/; HttpOnly; SameSite=Lax",
				loginResponse.token(), 7200);
		response.addHeader("Set-Cookie", cookieValue);

		return ResponseEntity.ok(loginResponse);
	}

	@PostMapping("/auth/forgot-password")
	@Operation(summary = "Request a password reset link", description = "Always returns 200 to prevent email enumeration.")
	public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPassword(request);
		return ResponseEntity.ok(Map.of("message", "Si cet email existe, un lien de réinitialisation a été envoyé."));
	}

	@PostMapping("/auth/reset-password")
	@Operation(summary = "Reset password using a valid token")
	public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès."));
	}

	@PostMapping("/auth/verify-account")
	@Operation(summary = "Verify a new account", description = "Finds the user by verification token, sets the password and activates the account.")
	@ApiResponse(responseCode = "200", description = "Account verified", content = @Content(examples = @ExampleObject("{\"message\": \"Account verified successfully\"}")))
	@ApiResponse(responseCode = "400", description = "Invalid token")
	@ApiResponse(responseCode = "404", description = "User not found")
	public ResponseEntity<Map<String, String>> verifyAccount(@Valid @RequestBody VerifyRequest request) {
		authService.verifyAccount(request);
		return ResponseEntity.ok(Map.of("message", "Account verified successfully"));
	}
}
