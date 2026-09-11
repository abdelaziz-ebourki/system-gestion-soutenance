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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api")
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

	private static final String ACCESS_COOKIE = "jwt_token";
	private static final String REFRESH_COOKIE = "refresh_token";
	private static final String REFRESH_PATH = "/api/auth";

	private final AuthService authService;
	private final boolean cookieSecure;

	public AuthController(AuthService authService, @Value("${app.cookie.secure:false}") boolean cookieSecure) {
		this.authService = authService;
		this.cookieSecure = cookieSecure;
	}

	@PostMapping("/auth/login")
	@Operation(summary = "Authenticate a user", description = "Validates credentials and returns user info. JWT access and refresh tokens are set as HTTP-only cookies.")
	@SecurityRequirements
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = LoginCookieResponse.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid email or password", content = @Content(examples = @ExampleObject("{\"message\": \"Invalid credentials (email or password incorrect)\"}")))})
	public ResponseEntity<LoginCookieResponse> login(@Valid @RequestBody LoginRequest request,
			HttpServletResponse response) {
		LoginResponse loginResponse = authService.login(request);

		long accessMaxAge = Math.max(60, (loginResponse.expiresAt() - System.currentTimeMillis()) / 1000);
		long refreshMaxAge = Math.max(60, (loginResponse.refreshExpiresAt() - System.currentTimeMillis()) / 1000);
		response.setHeader(HttpHeaders.SET_COOKIE, accessCookie(loginResponse.token(), accessMaxAge).toString());
		response.addHeader(HttpHeaders.SET_COOKIE,
				refreshCookie(loginResponse.refreshToken(), refreshMaxAge).toString());

		return ResponseEntity.ok(new LoginCookieResponse(loginResponse.user(), loginResponse.expiresAt()));
	}

	@PostMapping("/auth/refresh")
	@Operation(summary = "Rotate session tokens", description = "Reads the refresh cookie, rotates the token family and returns fresh cookies.")
	@SecurityRequirements
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tokens rotated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")})
	public ResponseEntity<LoginCookieResponse> refresh(
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken, HttpServletResponse response) {
		if (refreshToken == null || refreshToken.isBlank()) {
			return ResponseEntity.status(401).build();
		}
		LoginResponse loginResponse = authService.refresh(refreshToken);

		long accessMaxAge = Math.max(60, (loginResponse.expiresAt() - System.currentTimeMillis()) / 1000);
		long refreshMaxAge = Math.max(60, (loginResponse.refreshExpiresAt() - System.currentTimeMillis()) / 1000);
		response.setHeader(HttpHeaders.SET_COOKIE, accessCookie(loginResponse.token(), accessMaxAge).toString());
		response.addHeader(HttpHeaders.SET_COOKIE,
				refreshCookie(loginResponse.refreshToken(), refreshMaxAge).toString());

		return ResponseEntity.ok(new LoginCookieResponse(loginResponse.user(), loginResponse.expiresAt()));
	}

	@PostMapping("/auth/logout")
	@Operation(summary = "Logout", description = "Revokes the refresh token server-side and clears both cookies.")
	@SecurityRequirements
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Logged out successfully")})
	public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
			HttpServletResponse response) {
		authService.logout(refreshToken);
		response.setHeader(HttpHeaders.SET_COOKIE, accessCookie("", 0).toString());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie("", 0).toString());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/auth/forgot-password")
	@Operation(summary = "Request a password reset link", description = "Always returns 200 to prevent email enumeration.")
	@SecurityRequirements
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset link sent if email exists"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid email format")})
	public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPassword(request);
		return ApiResponse.success("Si cet email existe, un lien de réinitialisation a été envoyé.", null);
	}

	@PostMapping("/auth/reset-password")
	@Operation(summary = "Reset password using a valid token")
	@SecurityRequirements
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")})
	public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return ApiResponse.success("Mot de passe réinitialisé avec succès.", null);
	}

	@PostMapping("/auth/verify-account")
	@Operation(summary = "Verify a new account", description = "Finds the user by verification token, sets the password and activates the account.")
	@SecurityRequirements
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account verified", content = @Content(examples = @ExampleObject("{\"message\": \"Account verified successfully\"}"))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")})
	public ApiResponse<Void> verifyAccount(@Valid @RequestBody VerifyRequest request) {
		authService.verifyAccount(request);
		return ApiResponse.success("Compte vérifié avec succès.", null);
	}

	private ResponseCookie accessCookie(String value, long maxAgeSeconds) {
		return cookie(ACCESS_COOKIE, "/", value, maxAgeSeconds);
	}

	private ResponseCookie refreshCookie(String value, long maxAgeSeconds) {
		return cookie(REFRESH_COOKIE, REFRESH_PATH, value, maxAgeSeconds);
	}

	private ResponseCookie cookie(String name, String path, String value, long maxAgeSeconds) {
		return ResponseCookie.from(name, value).path(path).httpOnly(true).secure(cookieSecure).sameSite("Lax")
				.maxAge(Duration.ofSeconds(maxAgeSeconds)).build();
	}
}
