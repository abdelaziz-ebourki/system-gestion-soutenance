package com.system_gestion_soutenance.api.student.document.controller;

import com.system_gestion_soutenance.api.student.document.dto.StudentDocumentDto;
import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.service.StudentDocumentService;
import com.system_gestion_soutenance.api.common.mapper.StudentDocumentMapper;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/student/documents")
@Tag(name = "Student - Documents", description = "Gestion des documents de soutenance")
public class StudentDocumentController {

	private final StudentDocumentService studentDocumentService;
	private final StudentDocumentMapper mapper;

	public StudentDocumentController(StudentDocumentService studentDocumentService, StudentDocumentMapper mapper) {
		this.studentDocumentService = studentDocumentService;
		this.mapper = mapper;
	}

	@GetMapping
	@Operation(summary = "List documents for the connected student")
	public List<StudentDocumentDto> findByStudent() {
		return studentDocumentService.findByStudent(getCurrentUserId()).stream().map(mapper::toDto).toList();
	}

	@PostMapping("/{id}/upload")
	@Operation(summary = "Upload a document file")
	public ResponseEntity<StudentDocumentDto> upload(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
		StudentDocument doc = studentDocumentService.upload(id, file);
		return ResponseEntity.ok(mapper.toDto(doc));
	}

	private Long getCurrentUserId() {
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
	}
}
