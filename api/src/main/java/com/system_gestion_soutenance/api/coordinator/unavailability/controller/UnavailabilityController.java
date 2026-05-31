package com.system_gestion_soutenance.api.coordinator.unavailability.controller;

import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coordinator/unavailability")
@Tag(name = "Coordinator - Unavailability", description = "Consultation des indisponibilités des enseignants")
public class UnavailabilityController {

	private final UnavailabilityRepository repository;

	public UnavailabilityController(UnavailabilityRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	@Operation(summary = "List all unavailability records")
	public List<Unavailability> findAll() {
		return repository.findAll();
	}
}
