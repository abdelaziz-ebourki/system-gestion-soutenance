package com.system_gestion_soutenance.api.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppNotification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationType type;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String message;

	@Column(nullable = false)
	private LocalDateTime timestamp;

	@Column(nullable = false)
	private boolean read;

	@Column(name = "action_link")
	private String actionLink;

	private String actor;
}
