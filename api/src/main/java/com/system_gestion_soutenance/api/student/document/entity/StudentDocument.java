package com.system_gestion_soutenance.api.student.document.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "student_id", nullable = false)
	private Long studentId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String type;

	@Column(nullable = false)
	private String deadline;

	@Column(nullable = false)
	private String status = "missing";

	@Column(name = "submitted_at")
	private LocalDateTime submittedAt;

	@Column(name = "file_path")
	private String filePath;
}
