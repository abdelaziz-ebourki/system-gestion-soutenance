package com.system_gestion_soutenance.api.admin.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "Request to create multiple rooms in bulk")
public record BulkRoomRequest(@Schema(description = "List of rooms to create") @NotEmpty List<@Valid RoomEntry> rooms) {

	@Schema(description = "Details of a single room entry")
	public record RoomEntry(@Schema(description = "Name of the room", example = "Amphitheater A") @NotBlank String name,
			@Schema(description = "Seating capacity of the room", example = "50") @Positive int capacity,
			@Schema(description = "ID of the department this room belongs to", example = "1") @NotNull Long departmentId) {
	}
}
