package com.system_gestion_soutenance.api.notification.controller;

import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Gestion des notifications")
public class NotificationController {

	private final NotificationRepository repository;

	public NotificationController(NotificationRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	@Operation(summary = "List all notifications")
	public List<AppNotification> findAll() {
		return repository.findAllByOrderByTimestampDesc();
	}

	@PatchMapping("/{id}/read")
	@Operation(summary = "Mark a notification as read")
	public ResponseEntity<Void> markRead(@PathVariable Long id) {
		repository.findById(id).ifPresent(n -> {
			n.setRead(true);
			repository.save(n);
		});
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/read-all")
	@Operation(summary = "Mark all notifications as read")
	public ResponseEntity<Void> markAllRead() {
		List<AppNotification> all = repository.findAll();
		for (AppNotification n : all) {
			n.setRead(true);
		}
		repository.saveAll(all);
		return ResponseEntity.noContent().build();
	}
}
