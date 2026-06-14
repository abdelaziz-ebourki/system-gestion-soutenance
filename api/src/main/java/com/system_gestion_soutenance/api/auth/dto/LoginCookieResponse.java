package com.system_gestion_soutenance.api.auth.dto;

import com.system_gestion_soutenance.api.user.dto.UserDto;

public record LoginCookieResponse(UserDto user, long expiresAt) {
}
