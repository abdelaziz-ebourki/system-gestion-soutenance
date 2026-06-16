package com.system_gestion_soutenance.api.admin.defensesession.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.*;
@SuppressWarnings("PMD")

@Entity
@Table(name = "defense_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

	@Column(name = "approved_by")
	private Long approvedBy;

	@Column(name = "approved_at")
	private LocalDateTime approvedAt;

	@Column(name = "start_time")
	private String startTime;

	@Column(name = "end_time")
	private String endTime;

	@Column(name = "group_creation_start_date")
	private String groupCreationStartDate;

	@Column(name = "group_creation_end_date")
	private String groupCreationEndDate;

	public Long getJuryRoleTemplateId() {
		return juryRoleTemplate != null ? juryRoleTemplate.getId() : null;
	}
}