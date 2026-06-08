package com.system_gestion_soutenance.api.coordinator.defense.entity;

import com.system_gestion_soutenance.api.user.entity.Teacher;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JuryMember {

	@ManyToOne
	@JoinColumn(name = "teacher_id", nullable = false)
	private Teacher teacher;

	@jakarta.persistence.Column(name = "role_name", nullable = false)
	private String roleName;
}
