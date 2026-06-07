package com.system_gestion_soutenance.api.admin.room.controller;

import com.system_gestion_soutenance.api.admin.room.dto.BulkRoomRequest;
import com.system_gestion_soutenance.api.admin.room.dto.CreateRoomRequest;
import com.system_gestion_soutenance.api.admin.room.dto.RoomResponse;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.service.RoomService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.RoomMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/rooms")
@Tag(name = "Admin - Room Management", description = "Endpoints for managing defense rooms")
public class RoomController {

	private final RoomService roomService;
	private final RoomMapper roomMapper;

	public RoomController(RoomService roomService, RoomMapper roomMapper) {
		this.roomService = roomService;
		this.roomMapper = roomMapper;
	}

	@GetMapping
	@Operation(summary = "List rooms", description = "Retrieves a paginated list of available rooms.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved rooms")})
	public ApiResponse<PaginatedResponse<RoomResponse>> findAll(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit) {
		PaginatedResponse<Room> result = roomService.findAll(page, limit);
		List<RoomResponse> items = result.items().stream().map(roomMapper::toDto).toList();
		PaginatedResponse<RoomResponse> mapped = new PaginatedResponse<>(items, result.total(), result.pageCount(),
				result.currentPage(), result.size());
		return ApiResponse.success("Liste des salles récupérée avec succès", mapped);
	}

	@PostMapping
	@Operation(summary = "Create room", description = "Creates a new room.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Room created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid room data")})
	public ResponseEntity<ApiResponse<RoomResponse>> create(@Valid @RequestBody CreateRoomRequest request) {
		Room room = roomService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Salle créée avec succès", roomMapper.toDto(room)));
	}

	@PostMapping("/bulk")
	@Operation(summary = "Bulk create rooms", description = "Creates multiple rooms in a single request.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Rooms created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid bulk data")})
	public ResponseEntity<ApiResponse<List<RoomResponse>>> bulkCreate(@Valid @RequestBody BulkRoomRequest request) {
		List<Room> rooms = roomService.bulkCreate(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Salles créées avec succès", rooms.stream().map(roomMapper::toDto).toList()));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update room", description = "Updates an existing room's details.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Room updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<RoomResponse> update(@PathVariable Long id, @Valid @RequestBody CreateRoomRequest request) {
		return ApiResponse.success("Salle mise à jour avec succès", roomMapper.toDto(roomService.update(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete room", description = "Removes a room from the system.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Room deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found")})
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		roomService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Salle supprimée avec succès", null));
	}
}
