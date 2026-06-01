package com.system_gestion_soutenance.api.notification.controller;

import com.system_gestion_soutenance.api.notification.dto.AppNotificationDto;
import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import com.system_gestion_soutenance.api.common.mapper.AppNotificationMapper;
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
	private final AppNotificationMapper mapper;

	public NotificationController(NotificationRepository repository, AppNotificationMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@GetMapping
	@Operation(summary = "List all notifications")
	public List<AppNotificationDto> findAll() {
		return repository.findAllByOrderByTimestampDesc().stream().map(mapper::toDto).toList();
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
