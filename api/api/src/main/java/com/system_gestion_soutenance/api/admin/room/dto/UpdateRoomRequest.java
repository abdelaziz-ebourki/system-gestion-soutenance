package com.system_gestion_soutenance.api.admin.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update an existing room")
@SuppressWarnings("PMD")
public record UpdateRoomRequest(@Schema(description = "Name of the room", example = "Amphitheater A") String name,
		@Schema(description = "Seating capacity of the room", example = "50") Integer capacity,
		@Schema(description = "ID of the department this room belongs to", example = "1") Long departmentId) {
}
