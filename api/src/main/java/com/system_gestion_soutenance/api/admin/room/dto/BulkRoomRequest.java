package com.system_gestion_soutenance.api.admin.room.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record BulkRoomRequest(@NotEmpty List<@Valid RoomEntry> rooms) {
	public record RoomEntry(@NotBlank String name, @Positive int capacity, @NotNull Long departmentId) {
	}
}
