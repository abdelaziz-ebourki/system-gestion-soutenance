package com.system_gestion_soutenance.api.coordinator.group.document;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedException;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Group - Document Management", description = "Endpoints for group-level documents (Rapport, Présentation, Divers)")
public class GroupDocumentController {

	private final GroupDocumentService groupDocumentService;

	public GroupDocumentController(GroupDocumentService groupDocumentService) {
		this.groupDocumentService = groupDocumentService;
	}

	@GetMapping("/{id}/documents")
	@PreAuthorize("hasRole('COORDINATOR') or hasRole('STUDENT')")
	@Operation(summary = "List group documents", description = "Retrieves the 3 fixed documents (Rapport, Présentation, Divers) for a group.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved documents")})
	public ApiResponse<List<GroupDocumentDto>> findByGroup(@Parameter(description = "Group ID") @PathVariable Long id) {
		List<GroupDocument> docs = groupDocumentService.findByGroup(id);
		List<GroupDocumentDto> items = docs.stream().map(this::toDto).toList();
		return ApiResponse.success(items);
	}

	@PostMapping("/{id}/documents/{type}/attachments")
	@PreAuthorize("hasRole('STUDENT')")
	@Operation(summary = "Upload document", description = "Uploads a file for a specific document type. Only the group leader can upload.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File uploaded successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or request"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Group or document not found")})
	public ResponseEntity<ApiResponse<GroupDocumentDto>> upload(
			@Parameter(description = "Group ID") @PathVariable Long id,
			@Parameter(description = "Document type") @PathVariable GroupDocumentType type,
			@Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal User user) {
		if (user == null) {
			throw new UnauthorizedException("User not authenticated");
		}
		GroupDocument doc = groupDocumentService.upload(id, type, user.getId(), file);
		return ResponseEntity.ok(ApiResponse.success("Fichier téléchargé avec succès", toDto(doc)));
	}

	@GetMapping("/{id}/documents/{type}/download")
	@PreAuthorize("hasRole('COORDINATOR') or hasRole('STUDENT')")
	@Operation(summary = "Download document", description = "Downloads the submitted file for a specific document type.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File downloaded successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document or file not found")})
	public ResponseEntity<byte[]> download(@Parameter(description = "Group ID") @PathVariable Long id,
			@Parameter(description = "Document type") @PathVariable GroupDocumentType type) {
		byte[] content;
		try {
			content = groupDocumentService.download(id, type);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment",
				"document-" + id + "-" + type.name().toLowerCase() + ".bin");
		return new ResponseEntity<>(content, headers, HttpStatus.OK);
	}

	private GroupDocumentDto toDto(GroupDocument doc) {
		return new GroupDocumentDto(doc.getId(), doc.getGroupId(), doc.getType(), doc.getName(), doc.getDeadline(),
				doc.getStatus(), doc.getSubmittedAt(), doc.getFilePath());
	}
}
