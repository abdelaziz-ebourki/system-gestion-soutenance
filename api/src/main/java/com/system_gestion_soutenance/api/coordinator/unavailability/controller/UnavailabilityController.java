package com.system_gestion_soutenance.api.coordinator.unavailability.controller;

import com.system_gestion_soutenance.api.coordinator.unavailability.dto.UnavailabilityDto;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import com.system_gestion_soutenance.api.common.mapper.UnavailabilityMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/coordinator/unavailability")
@PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
@Tag(name = "Coordinator - Unavailability", description = "Teacher Unavailability")
public class UnavailabilityController {

	private final UnavailabilityRepository repository;
	private final UnavailabilityMapper mapper;

	public UnavailabilityController(UnavailabilityRepository repository, UnavailabilityMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@GetMapping
	@Operation(summary = "List all unavailability records")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully retrieved unavailability records"),
			@ApiResponse(responseCode = "500", description = "Internal server error")})
	public List<UnavailabilityDto> findAll() {
		return repository.findAll().stream().map(mapper::toDto).toList();
	}
}
