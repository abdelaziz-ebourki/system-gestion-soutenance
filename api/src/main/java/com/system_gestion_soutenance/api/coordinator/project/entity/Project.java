package com.system_gestion_soutenance.api.coordinator.project.entity;

import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
@SuppressWarnings("PMD")

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "defense_type", nullable = false)
	private String defenseType;

	@Column(name = "max_students")
	private Integer maxStudents;

	@Column(name = "proposed_by_teacher_id")
	private Long proposedByTeacherId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProjectStatus status = ProjectStatus.PENDING;

	@ManyToOne
	@JoinColumn(name = "supervisor_id")
	private Teacher supervisor;

	@ManyToMany
	@JoinTable(name = "project_supervisors", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "teacher_id"))
	private List<Teacher> coSupervisors;

	@OneToOne(mappedBy = "project")
	private Defense defense;
}