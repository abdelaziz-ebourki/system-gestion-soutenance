package com.system_gestion_soutenance.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
@SuppressWarnings("PMD")

@Schema(description = "Request to initiate password reset")
public record ForgotPasswordRequest(
		@NotBlank @Email @Schema(description = "Registered email address", example = "user@univ-h2.ma") String email) {
}
