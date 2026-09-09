package com.system_gestion_soutenance.api.coordinator.room.controller;

import com.system_gestion_soutenance.api.admin.room.dto.RoomResponse;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.service.RoomService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.RoomMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/coordinator/rooms")
@PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
@Tag(name = "Coordinator - Rooms", description = "Endpoints for coordinators to view rooms")
public class CoordinatorRoomController {

	private final RoomService roomService;
	private final RoomMapper roomMapper;

	public CoordinatorRoomController(RoomService roomService, RoomMapper roomMapper) {
		this.roomService = roomService;
		this.roomMapper = roomMapper;
	}

	@GetMapping
	@Operation(summary = "List rooms", description = "Retrieves a paginated list of available rooms.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved rooms")})
	public ApiResponse<PaginatedResponse<RoomResponse>> findAll(
			@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
			@Parameter(description = "Items per page (1-500)") @RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		PaginatedResponse<Room> result = roomService.findAll(page, limit);
		List<RoomResponse> items = result.items().stream().map(roomMapper::toDto).toList();
		PaginatedResponse<RoomResponse> mapped = new PaginatedResponse<>(items, result.total(), result.pageCount(),
				result.currentPage(), result.size());
		return ApiResponse.success("Liste des salles récupérée avec succès", mapped);
	}
}
