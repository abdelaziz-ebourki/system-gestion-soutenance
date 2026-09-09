package com.system_gestion_soutenance.api.auth.dto;

import com.system_gestion_soutenance.api.user.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
@SuppressWarnings("PMD")

@Schema(description = "Internal login result (includes token for cookie)")
public record LoginResponse(@Schema(description = "Authenticated user details") UserDto user,
		@Schema(description = "JWT token") String token,
		@Schema(description = "Token expiry time in milliseconds since epoch") long expiresAt) {
}
