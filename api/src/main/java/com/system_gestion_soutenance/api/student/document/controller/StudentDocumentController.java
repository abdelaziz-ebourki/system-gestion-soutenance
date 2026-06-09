package com.system_gestion_soutenance.api.student.document.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.StudentDocumentMapper;
import com.system_gestion_soutenance.api.student.document.dto.StudentDocumentDto;
import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.service.StudentDocumentService;
import com.system_gestion_soutenance.api.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/student/documents")
@Tag(name = "Student - Document Management", description = "Endpoints for students to upload and view their documents")
public class StudentDocumentController {

	private final StudentDocumentService studentDocumentService;
	private final StudentDocumentMapper mapper;

	public StudentDocumentController(StudentDocumentService studentDocumentService, StudentDocumentMapper mapper) {
		this.studentDocumentService = studentDocumentService;
		this.mapper = mapper;
	}

	@GetMapping
	@Operation(summary = "List documents", description = "Retrieves all documents uploaded by the connected student.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved documents")})
	public ApiResponse<List<StudentDocumentDto>> findByStudent(@AuthenticationPrincipal User user) {
		return ApiResponse
				.success(studentDocumentService.findByStudent(user.getId()).stream().map(mapper::toDto).toList());
	}

	@PostMapping("/{id}/attachments")
	@Operation(summary = "Upload attachment", description = "Uploads a file as an attachment to a specific document.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File uploaded successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or request"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")})
	public ResponseEntity<ApiResponse<StudentDocumentDto>> upload(@PathVariable Long id,
			@RequestParam("file") MultipartFile file) {
		StudentDocument doc = studentDocumentService.upload(id, file);
		return ResponseEntity.ok(ApiResponse.success(mapper.toDto(doc)));
	}

	@GetMapping("/{id}/download")
	@Operation(summary = "Download document file", description = "Downloads the submitted file for a document.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File downloaded successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document or file not found")})
	public ResponseEntity<byte[]> download(@PathVariable Long id) {
		byte[] content;
		try {
			content = studentDocumentService.download(id);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", "document-" + id + ".bin");
		return new ResponseEntity<>(content, headers, HttpStatus.OK);
	}
}
