package com.system_gestion_soutenance.api.coordinator.group.entity;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.user.entity.Student;
import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
@SuppressWarnings("PMD")

@Entity
@Table(name = "coordinator_group")
@Getter
@Setter
@NoArgsConstructor
public class Group {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "group_name", unique = true)
	private String groupName;

	@ManyToOne
	@JoinColumn(name = "project_id")
	private Project project;

	@ManyToMany
	@JoinTable(name = "group_members", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "student_id"))
	private List<Student> students;

	@ManyToOne
	@JoinColumn(name = "session_id")
	private DefenseSession defenseSession;

	@Column(name = "leader_id")
	private Long leaderId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GroupStatus status = GroupStatus.PENDING;
}