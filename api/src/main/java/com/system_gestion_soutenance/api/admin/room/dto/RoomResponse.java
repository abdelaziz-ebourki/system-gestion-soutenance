package com.system_gestion_soutenance.api.admin.room.dto;

import com.system_gestion_soutenance.api.admin.room.entity.Room;

public record RoomResponse(Long id, String name, int capacity, Long departmentId) {
	public static RoomResponse from(Room room) {
		Long deptId = room.getDepartment() != null ? room.getDepartment().getId() : null;
		return new RoomResponse(room.getId(), room.getName(), room.getCapacity(), deptId);
	}
}
