package com.system_gestion_soutenance.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
@SuppressWarnings("PMD")

@Schema(description = "Login credentials")
public record LoginRequest(
		@NotBlank @Email @Schema(description = "User email address", example = "user@univ-h2.ma") String email,
		@NotBlank @Schema(description = "User password", example = "securePass123") String password) {
}
