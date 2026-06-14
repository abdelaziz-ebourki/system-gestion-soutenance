package com.system_gestion_soutenance.api.auth.controller;

import com.system_gestion_soutenance.api.auth.dto.ForgotPasswordRequest;
import com.system_gestion_soutenance.api.auth.dto.LoginCookieResponse;
import com.system_gestion_soutenance.api.auth.dto.LoginRequest;
import com.system_gestion_soutenance.api.auth.dto.LoginResponse;
import com.system_gestion_soutenance.api.auth.dto.ResetPasswordRequest;
import com.system_gestion_soutenance.api.auth.dto.VerifyRequest;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api")
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/auth/login")
	@Operation(summary = "Authenticate a user", description = "Validates credentials and returns user info. JWT is set as an HTTP-only cookie.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = LoginCookieResponse.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid email or password", content = @Content(examples = @ExampleObject("{\"message\": \"Invalid credentials (email or password incorrect)\"}")))
	public ResponseEntity<LoginCookieResponse> login(@Valid @RequestBody LoginRequest request,
			HttpServletResponse response) {
		LoginResponse loginResponse = authService.login(request);

		ResponseCookie jwtCookie = ResponseCookie.from("jwt_token", loginResponse.token())
				.path("/")
				.httpOnly(true)
				.secure(true)
				.sameSite("None")
				.maxAge(7200)
				.build();
		response.setHeader("Set-Cookie", jwtCookie.toString());

		return ResponseEntity.ok(new LoginCookieResponse(loginResponse.user(), loginResponse.expiresAt()));
	}

	@PostMapping("/auth/logout")
	@Operation(summary = "Logout", description = "Clears the JWT cookie.")
	public ResponseEntity<Void> logout(HttpServletResponse response) {
		ResponseCookie jwtCookie = ResponseCookie.from("jwt_token", "")
				.path("/")
				.httpOnly(true)
				.secure(true)
				.sameSite("None")
				.maxAge(0)
				.build();
		response.setHeader("Set-Cookie", jwtCookie.toString());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/auth/forgot-password")
	@Operation(summary = "Request a password reset link", description = "Always returns 200 to prevent email enumeration.")
	public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPassword(request);
		return ApiResponse.success("Si cet email existe, un lien de réinitialisation a été envoyé.", null);
	}

	@PostMapping("/auth/reset-password")
	@Operation(summary = "Reset password using a valid token")
	public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return ApiResponse.success("Mot de passe réinitialisé avec succès.", null);
	}

	@PostMapping("/auth/verify-account")
	@Operation(summary = "Verify a new account", description = "Finds the user by verification token, sets the password and activates the account.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account verified", content = @Content(examples = @ExampleObject("{\"message\": \"Account verified successfully\"}")))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
	public ApiResponse<Void> verifyAccount(@Valid @RequestBody VerifyRequest request) {
		authService.verifyAccount(request);
		return ApiResponse.success("Compte vérifié avec succès.", null);
	}
}