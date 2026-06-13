package com.system_gestion_soutenance.api.notification.controller;

import com.system_gestion_soutenance.api.notification.dto.AppNotificationDto;
import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import com.system_gestion_soutenance.api.notification.service.NotificationService;
import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.AppNotificationMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification Management")
public class NotificationController {

	private final NotificationService notificationService;
	private final NotificationRepository repository;
	private final AppNotificationMapper mapper;

	public NotificationController(NotificationService notificationService, NotificationRepository repository,
			AppNotificationMapper mapper) {
		this.notificationService = notificationService;
		this.repository = repository;
		this.mapper = mapper;
	}

	@GetMapping
	@Operation(summary = "List all notifications")
	public ApiResponse<PaginatedResponse<AppNotificationDto>> findAll(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(500) int limit) {
		PaginatedResponse<AppNotification> result = notificationService.findAll(page, limit);
		List<AppNotificationDto> items = result.items().stream().map(mapper::toDto).toList();
		PaginatedResponse<AppNotificationDto> mapped = new PaginatedResponse<>(items, result.total(),
				result.pageCount(), result.currentPage(), result.size());
		return ApiResponse.success(mapped);
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

	@PostMapping("/{id}/send-email")
	@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
	@Operation(summary = "Manually trigger email delivery for a notification")
	public ResponseEntity<Void> sendEmail(@PathVariable Long id) {
		notificationService.sendNotificationEmail(id);
		return ResponseEntity.noContent().build();
	}
}