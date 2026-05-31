package com.system_gestion_soutenance.api.admin.config.email.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateEmailConfigRequest(@NotBlank String host, @Min(1) int port, String username, String password,
		String senderName, String senderEmail, String encryption) {
}
