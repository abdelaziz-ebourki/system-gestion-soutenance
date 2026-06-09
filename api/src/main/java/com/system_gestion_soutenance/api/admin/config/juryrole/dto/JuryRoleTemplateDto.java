package com.system_gestion_soutenance.api.admin.config.juryrole.dto;

import java.util.List;
@SuppressWarnings("PMD")

public record JuryRoleTemplateDto(Long id, String name, String defenseType, List<TemplateRoleDto> roles) {
}