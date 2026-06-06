package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.admin.room.dto.RoomResponse;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface RoomMapper {
	@Mapping(target = "departmentId", source = "department.id")
	RoomResponse toDto(Room room);
}
