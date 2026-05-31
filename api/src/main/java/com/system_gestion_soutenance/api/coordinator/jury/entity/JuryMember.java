package com.system_gestion_soutenance.api.coordinator.jury.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

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
	@JoinColumn(name = "jury_id", nullable = false)
	@JsonIgnore
	private Jury jury;

	@Column(name = "role_name", nullable = false)
	private String roleName;

	@ManyToOne
	@JoinColumn(name = "teacher_id", nullable = false)
	private Teacher teacher;

	public Long getTeacherId() {
		return teacher != null ? teacher.getId() : null;
	}
}
