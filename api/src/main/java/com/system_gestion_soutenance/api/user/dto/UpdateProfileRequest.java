package com.system_gestion_soutenance.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
@SuppressWarnings("PMD")

@Schema(description = "Request to update the authenticated user's profile (self-service)")
public record UpdateProfileRequest(@Schema(description = "Last name", example = "Doe") String lastName,
		@Schema(description = "First name", example = "John") String firstName) {
}
