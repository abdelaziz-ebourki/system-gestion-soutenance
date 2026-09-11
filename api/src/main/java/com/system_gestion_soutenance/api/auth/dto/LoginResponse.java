package com.system_gestion_soutenance.api.auth.dto;

import com.system_gestion_soutenance.api.user.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
@SuppressWarnings("PMD")

@Schema(description = "Internal login result (includes tokens for cookies)")
public record LoginResponse(@Schema(description = "Authenticated user details") UserDto user,
		@Schema(description = "JWT access token") String token,
		@Schema(description = "Access token expiry time in milliseconds since epoch") long expiresAt,
		@Schema(description = "Opaque refresh token") String refreshToken,
		@Schema(description = "Refresh token expiry time in milliseconds since epoch") long refreshExpiresAt) {
}
