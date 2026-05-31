package com.system_gestion_soutenance.api.admin.faculty.controller;

import com.system_gestion_soutenance.api.admin.faculty.dto.CreateFacultyRequest;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.faculty.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/faculties")
@Tag(name = "Admin - Faculties", description = "Gestion des facultés")
public class FacultyController {

	private final FacultyService facultyService;

	public FacultyController(FacultyService facultyService) {
		this.facultyService = facultyService;
	}

	@GetMapping
	@Operation(summary = "List all faculties")
	public List<Faculty> findAll() {
		return facultyService.findAll();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a faculty by ID")
	public Faculty findById(@PathVariable Long id) {
		return facultyService.findById(id);
	}

	@PostMapping
	@Operation(summary = "Create a new faculty")
	public ResponseEntity<Faculty> create(@Valid @RequestBody CreateFacultyRequest request) {
		Faculty faculty = facultyService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(faculty);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a faculty")
	public Faculty update(@PathVariable Long id, @Valid @RequestBody CreateFacultyRequest request) {
		return facultyService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a faculty")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		facultyService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
