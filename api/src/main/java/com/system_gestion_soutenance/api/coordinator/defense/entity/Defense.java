package com.system_gestion_soutenance.api.coordinator.defense.entity;

import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
@SuppressWarnings("PMD")

@Entity
@Table(name = "defense")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Defense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "project_id", nullable = false, unique = true)
	private Project project;

	@Column(nullable = false)
	private LocalDate date;

	@Column(nullable = false)
	private LocalTime time;

	@ManyToOne
	@JoinColumn(name = "room_id")
	private Room room;

	@ElementCollection
	@CollectionTable(name = "defense_members", joinColumns = @JoinColumn(name = "defense_id"))
	private List<JuryMember> members = new ArrayList<>();

	public Long getProjectId() {
		return project != null ? project.getId() : null;
	}
}