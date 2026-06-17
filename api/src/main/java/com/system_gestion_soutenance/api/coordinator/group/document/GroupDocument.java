package com.system_gestion_soutenance.api.coordinator.group.document;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "group_id", nullable = false)
	private Long groupId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GroupDocumentType type;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private LocalDate deadline;

	@Column(nullable = false)
	private String status = "missing";

	@Column(name = "submitted_at")
	private LocalDateTime submittedAt;

	@Column(name = "file_path")
	private String filePath;
}
