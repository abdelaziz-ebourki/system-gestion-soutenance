package com.system_gestion_soutenance.api.admin.room.dto;

import com.system_gestion_soutenance.api.admin.room.entity.Room;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing room details")
@SuppressWarnings("PMD")
public record RoomResponse(@Schema(description = "Unique identifier of the room", example = "1") Long id,
		@Schema(description = "Name of the room", example = "Amphitheater A") String name,
		@Schema(description = "Seating capacity of the room", example = "50") int capacity,
		@Schema(description = "ID of the department this room belongs to", example = "1") Long departmentId) {
	public static RoomResponse from(Room room) {
		Long deptId = room.getDepartment() != null ? room.getDepartment().getId() : null;
		return new RoomResponse(room.getId(), room.getName(), room.getCapacity(), deptId);
	}
}
