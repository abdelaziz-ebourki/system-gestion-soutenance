package com.system_gestion_soutenance.api.auth.dto;

import com.system_gestion_soutenance.api.user.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
@SuppressWarnings("PMD")

@Schema(description = "Response returned after successful login (JWT is set as HTTP-only cookie)")
public record LoginCookieResponse(@Schema(description = "Authenticated user details") UserDto user,
		@Schema(description = "Token expiry time in milliseconds since epoch") long expiresAt) {
}
