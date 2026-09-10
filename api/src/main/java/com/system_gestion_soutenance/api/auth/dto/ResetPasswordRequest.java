package com.system_gestion_soutenance.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
@SuppressWarnings("PMD")

@Schema(description = "Request to reset password using a token")
public record ResetPasswordRequest(
		@NotBlank @Schema(description = "Password reset token from email", example = "abc123-def456") String token,
		@NotBlank @Schema(description = "New password to set", example = "newSecurePass456") String password) {
}
