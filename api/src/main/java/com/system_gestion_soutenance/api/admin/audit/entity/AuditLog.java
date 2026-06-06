package com.system_gestion_soutenance.api.admin.audit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String action;

	@Column(nullable = false)
	private String entity;

	@Column(name = "entity_id")
	private Long entityId;

	@Column(name = "performed_by_email")
	private String performedByEmail;

	@Column(columnDefinition = "TEXT")
	private String details;

	@Column(nullable = false)
	private LocalDateTime timestamp;
}
