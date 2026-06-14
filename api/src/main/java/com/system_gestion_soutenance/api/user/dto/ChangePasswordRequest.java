package com.system_gestion_soutenance.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
@SuppressWarnings("PMD")

@Schema(description = "Request to change the authenticated user's password")
public record ChangePasswordRequest(
		@NotBlank @Schema(description = "Current password", example = "OldP@ss123") String currentPassword,
		@NotBlank @Schema(description = "New password", example = "NewP@ss456") String newPassword) {
}
