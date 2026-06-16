package com.system_gestion_soutenance.api.coordinator.defense.entity;

import com.system_gestion_soutenance.api.user.entity.Teacher;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "jury_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JuryMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "teacher_id", nullable = false)
	private Teacher teacher;

	@Column(name = "role_name", nullable = false)
	private String roleName;

	@ManyToOne
	@JoinColumn(name = "defense_id")
	private Defense defense;
}
