package com.system_gestion_soutenance.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
@SuppressWarnings("PMD")

@Schema(description = "Request to verify a new account")
public record VerifyRequest(
		@NotBlank @Schema(description = "Account verification token from email", example = "verify-token-abc123") String token,
		@NotBlank @Schema(description = "Password to set for the account", example = "securePass123") String password) {
}
