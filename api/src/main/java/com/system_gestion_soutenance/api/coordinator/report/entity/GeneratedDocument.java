package com.system_gestion_soutenance.api.coordinator.report.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
@SuppressWarnings("PMD")

@Entity
@Table(name = "generated_document")
@Getter
@Setter
@NoArgsConstructor
public class GeneratedDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String type;

	@Column(name = "generated_by", nullable = false)
	private Long generatedBy;

	@Column(name = "generated_at", nullable = false)
	private LocalDateTime generatedAt;

	@Column(name = "session_id")
	private Long sessionId;

	@Column(name = "file_size")
	private Long fileSize;
}
