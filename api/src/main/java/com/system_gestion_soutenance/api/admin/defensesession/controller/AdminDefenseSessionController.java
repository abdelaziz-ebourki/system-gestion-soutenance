package com.system_gestion_soutenance.api.admin.defensesession.controller;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/admin/sessions")
@Tag(name = "Admin - Defense Sessions", description = "Endpoints for admin to manage defense sessions")
public class AdminDefenseSessionController {

	private final DefenseSessionRepository defenseSessionRepository;
	private final SecurityService securityService;

	public AdminDefenseSessionController(DefenseSessionRepository defenseSessionRepository,
			SecurityService securityService) {
		this.defenseSessionRepository = defenseSessionRepository;
		this.securityService = securityService;
	}

	@PatchMapping("/{id}/approve-results")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Approve results", description = "Admin approves the deliberation results for a session, publishing them to students.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Results approved and published"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Session not yet deliberated"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")})
	public ApiResponse<Void> approveResults(@Parameter(description = "Session ID") @PathVariable Long id) {
		DefenseSession session = defenseSessionRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Session non trouvée"));

		if (session.getDeliberatedAt() == null) {
			throw new InvalidBusinessStateException("La session n'a pas encore été délibérée");
		}

		session.setValidatedBy(securityService.getCurrentUserId());
		session.setValidatedAt(LocalDateTime.now());
		session.setResultsPublished(true);
		defenseSessionRepository.save(session);

		return ApiResponse.success("Résultats approuvés et publiés avec succès", null);
	}
}
