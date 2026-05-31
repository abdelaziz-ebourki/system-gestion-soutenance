package com.system_gestion_soutenance.api.admin.department.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "department")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Department {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String code;

	@ManyToOne
	@JoinColumn(name = "head_id")
	@JsonIgnore
	private Teacher head;

	@ManyToOne
	@JoinColumn(name = "faculty_id")
	private Faculty faculty;

	public Long getHeadId() {
		return head != null ? head.getId() : null;
	}

	public Long getFacultyId() {
		return faculty != null ? faculty.getId() : null;
	}
}
