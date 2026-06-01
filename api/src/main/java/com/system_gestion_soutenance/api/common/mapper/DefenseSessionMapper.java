package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.dto.DefenseSessionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface DefenseSessionMapper {
	@Mapping(target = "juryRoleTemplateId", source = "juryRoleTemplate.id")
	DefenseSessionDto toDto(DefenseSession session);
}
