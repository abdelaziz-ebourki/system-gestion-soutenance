package com.system_gestion_soutenance.api.auth.dto;

import com.system_gestion_soutenance.api.user.dto.UserDto;

public record LoginResponse(UserDto user, String token, long expiresAt) {
}
