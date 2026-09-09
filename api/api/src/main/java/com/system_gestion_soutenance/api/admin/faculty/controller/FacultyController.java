package com.system_gestion_soutenance.api.admin.faculty.controller;

import com.system_gestion_soutenance.api.admin.faculty.dto.CreateFacultyRequest;
import com.system_gestion_soutenance.api.admin.faculty.dto.FacultyDto;
import com.system_gestion_soutenance.api.admin.faculty.dto.UpdateFacultyRequest;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.faculty.service.FacultyService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/admin/faculties")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Faculty Management", description = "Endpoints for managing academic faculties")
public class FacultyController {

	private final FacultyService facultyService;
	private final ConfigMapper configMapper;

	public FacultyController(FacultyService facultyService, ConfigMapper configMapper) {
		this.facultyService = facultyService;
		this.configMapper = configMapper;
	}

	@GetMapping
	@Operation(summary = "List faculties", description = "Retrieves all academic faculties.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved faculties")})
	public ApiResponse<PaginatedResponse<FacultyDto>> findAll(
			@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
			@Parameter(description = "Items per page (1-500)") @RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		PaginatedResponse<Faculty> result = facultyService.findAll(page, limit);
		List<FacultyDto> items = result.items().stream().map(configMapper::toFacultyDto).toList();
		PaginatedResponse<FacultyDto> mapped = new PaginatedResponse<>(items, result.total(), result.pageCount(),
				result.currentPage(), result.size());
		return ApiResponse.success("Liste des facultés récupérée avec succès", mapped);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get faculty", description = "Retrieves details of a specific faculty by its ID.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved faculty"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Faculty not found")})
	public ApiResponse<FacultyDto> findById(@Parameter(description = "Faculty ID") @PathVariable Long id) {
		return ApiResponse.success("Faculté récupérée avec succès",
				configMapper.toFacultyDto(facultyService.findById(id)));
	}

	@PostMapping
	@Operation(summary = "Create faculty", description = "Creates a new academic faculty.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Faculty created successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid faculty data")})
	public ResponseEntity<ApiResponse<FacultyDto>> create(@Valid @RequestBody CreateFacultyRequest request) {
		Faculty faculty = facultyService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Faculté créée avec succès", configMapper.toFacultyDto(faculty)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update faculty", description = "Updates an existing faculty's details.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Faculty updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Faculty not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<FacultyDto> update(@Parameter(description = "Faculty ID") @PathVariable Long id,
			@Valid @RequestBody CreateFacultyRequest request) {
		return ApiResponse.success("Faculté mise à jour avec succès",
				configMapper.toFacultyDto(facultyService.update(id, request)));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update faculty", description = "Updates only the provided fields of a faculty.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Faculty partially updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Faculty not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")})
	public ApiResponse<FacultyDto> patch(@Parameter(description = "Faculty ID") @PathVariable Long id,
			@Valid @RequestBody UpdateFacultyRequest request) {
		return ApiResponse.success("Faculté mise à jour avec succès",
				configMapper.toFacultyDto(facultyService.updatePartial(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete faculty", description = "Removes a faculty from the system.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Faculty deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Faculty not found")})
	public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "Faculty ID") @PathVariable Long id) {
		facultyService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Faculté supprimée avec succès", null));
	}
}
