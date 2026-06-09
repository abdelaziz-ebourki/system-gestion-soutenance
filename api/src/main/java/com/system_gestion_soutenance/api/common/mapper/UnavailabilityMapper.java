package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.coordinator.unavailability.dto.UnavailabilityDto;
import org.mapstruct.Mapper;
@SuppressWarnings("PMD")

@Mapper(config = CentralMapperConfig.class)
public interface UnavailabilityMapper {
	UnavailabilityDto toDto(Unavailability unavailability);
}