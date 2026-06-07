package com.system_gestion_soutenance.api.admin.faculty.controller;

import com.system_gestion_soutenance.api.admin.faculty.dto.CreateFacultyRequest;
import com.system_gestion_soutenance.api.admin.faculty.dto.FacultyDto;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.faculty.service.FacultyService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/faculties")
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
	public ApiResponse<List<FacultyDto>> findAll() {
		List<FacultyDto> faculties = facultyService.findAll().stream().map(configMapper::toFacultyDto).toList();
		return ApiResponse.success("Liste des facultés récupérée avec succès", faculties);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get faculty", description = "Retrieves details of a specific faculty by its ID.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved faculty"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Faculty not found")})
	public ApiResponse<FacultyDto> findById(@PathVariable Long id) {
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
	public ApiResponse<FacultyDto> update(@PathVariable Long id, @Valid @RequestBody CreateFacultyRequest request) {
		return ApiResponse.success("Faculté mise à jour avec succès",
				configMapper.toFacultyDto(facultyService.update(id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete faculty", description = "Removes a faculty from the system.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Faculty deleted successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Faculty not found")})
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		facultyService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Faculté supprimée avec succès", null));
	}
}
