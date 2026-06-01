package com.system_gestion_soutenance.api.admin.room.controller;

import com.system_gestion_soutenance.api.admin.room.dto.BulkRoomRequest;
import com.system_gestion_soutenance.api.admin.room.dto.CreateRoomRequest;
import com.system_gestion_soutenance.api.admin.room.dto.RoomResponse;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.service.RoomService;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/rooms")
@Tag(name = "Admin - Rooms", description = "Gestion des salles")
public class RoomController {

	private final RoomService roomService;

	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}

	@GetMapping
	@Operation(summary = "List all rooms with pagination")
	public PaginatedResponse<RoomResponse> findAll(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit) {
		return roomService.findAll(page, limit);
	}

	@PostMapping
	@Operation(summary = "Create a new room")
	public ResponseEntity<RoomResponse> create(@Valid @RequestBody CreateRoomRequest request) {
		Room room = roomService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(RoomResponse.from(room));
	}

	@PostMapping("/bulk")
	@Operation(summary = "Bulk create rooms")
	public ResponseEntity<List<RoomResponse>> bulkCreate(@Valid @RequestBody BulkRoomRequest request) {
		List<Room> rooms = roomService.bulkCreate(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(rooms.stream().map(RoomResponse::from).toList());
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a room")
	public RoomResponse update(@PathVariable Long id, @Valid @RequestBody CreateRoomRequest request) {
		return RoomResponse.from(roomService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a room")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		roomService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
