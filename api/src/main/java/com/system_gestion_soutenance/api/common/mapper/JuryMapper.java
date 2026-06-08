package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.coordinator.jury.dto.JuryResponse;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = JuryMemberMapper.class)
public interface JuryMapper {

	@Mapping(target = "projectId", source = "project.id")
	@Mapping(target = "projectTitle", source = "project.title")
	@Mapping(target = "defenseType", source = "project.defenseType")
	@Mapping(target = "members", source = "members")
	JuryResponse toDto(Defense defense);
}
