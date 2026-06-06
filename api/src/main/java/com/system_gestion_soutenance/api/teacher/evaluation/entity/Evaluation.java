package com.system_gestion_soutenance.api.teacher.evaluation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evaluation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "teacher_id", nullable = false)
	private Long teacherId;

	@Column(name = "defense_session_id", nullable = false)
	private Long defenseSessionId;

	@Column(name = "project_id", nullable = false)
	private Long projectId;

	@Column(nullable = false)
	private String role;

	private Double score;

	@Column(columnDefinition = "TEXT")
	private String comment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EvaluationStatus status = EvaluationStatus.PENDING;

	@Column(name = "submitted_at")
	private LocalDateTime submittedAt;
}
