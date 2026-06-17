package com.system_gestion_soutenance.api.admin.defensesession.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
@SuppressWarnings("PMD")

@Entity
@Table(name = "defense_session")
@Getter
@Setter
@NoArgsConstructor
public class DefenseSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "defense_type", nullable = false)
	private DefenseType defenseType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DefenseSessionStatus status = DefenseSessionStatus.DRAFT;

	@Column(name = "max_group_size")
	private int maxGroupSize;

	@Column(name = "defense_duration")
	private int defenseDuration;

	@Column(name = "break_duration")
	private int breakDuration;

	@Column(name = "submission_deadline")
	private LocalDate submissionDeadline;

	@ElementCollection
	@CollectionTable(name = "defense_session_coefficients", joinColumns = @JoinColumn(name = "defense_session_id"))
	@MapKeyColumn(name = "role_name")
	@Column(name = "coefficient")
	private Map<String, Integer> evaluationCoefficients = new HashMap<>();

	@ManyToOne
	@JoinColumn(name = "jury_role_template_id")
	@JsonIgnore
	private JuryRoleTemplate juryRoleTemplate;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Column(name = "is_frozen")
	private boolean isFrozen = false;

	@Column(name = "allow_supervisor_in_jury")
	private boolean allowSupervisorInJury = false;

	@Column(name = "results_published")
	private boolean resultsPublished = false;
	@Column(name = "approved_by")
	private Long approvedBy;

	@Column(name = "approved_at")
	private LocalDateTime approvedAt;

	@Column(name = "min_group_size")
	private int minGroupSize = 1;

	@Column(name = "group_formation_start_date")
	private LocalDate groupFormationStartDate;

	@Column(name = "group_formation_end_date")
	private LocalDate groupFormationEndDate;

	@Column(name = "start_time")
	private String startTime;

	@Column(name = "end_time")
	private String endTime;

	@Column(name = "group_creation_start_date")
	private String groupCreationStartDate;

	@Column(name = "group_creation_end_date")
	private String groupCreationEndDate;

	@Column(name = "rapport_coefficient")
	private int rapportCoefficient = 30;

	@Column(name = "soutenance_coefficient")
	private int soutenanceCoefficient = 70;

	@Column(name = "deliberated_by")
	private Long deliberatedBy;

	@Column(name = "deliberated_at")
	private LocalDateTime deliberatedAt;

	@Column(name = "validated_by")
	private Long validatedBy;

	@Column(name = "validated_at")
	private LocalDateTime validatedAt;

	public Long getJuryRoleTemplateId() {
		return juryRoleTemplate != null ? juryRoleTemplate.getId() : null;
	}
}