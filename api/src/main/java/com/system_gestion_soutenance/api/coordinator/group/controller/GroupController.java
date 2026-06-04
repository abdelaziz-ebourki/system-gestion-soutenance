package com.system_gestion_soutenance.api.coordinator.group.controller;

import com.system_gestion_soutenance.api.coordinator.group.dto.CreateGroupRequest;
import com.system_gestion_soutenance.api.coordinator.group.dto.GroupResponse;
import com.system_gestion_soutenance.api.coordinator.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coordinator/groups")
@Tag(name = "Coordinator - Groups", description = "Gestion des groupes d'étudiants")
public class GroupController {

	private final GroupService groupService;

	public GroupController(GroupService groupService) {
		this.groupService = groupService;
	}

	@GetMapping
	@Operation(summary = "List all groups")
	public List<GroupResponse> findAll() {
		return groupService.findAll();
	}

	@PostMapping
	@Operation(summary = "Create a new group")
	public ResponseEntity<GroupResponse> create(@Valid @RequestBody CreateGroupRequest request) {
		GroupResponse group = groupService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(group);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a group")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		groupService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
