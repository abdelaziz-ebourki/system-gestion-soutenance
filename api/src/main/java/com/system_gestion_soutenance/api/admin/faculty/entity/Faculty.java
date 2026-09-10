package com.system_gestion_soutenance.api.admin.faculty.entity;

import com.system_gestion_soutenance.api.user.entity.Teacher;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@SuppressWarnings("PMD")

@Entity
@Table(name = "faculty")
@Getter
@Setter
@NoArgsConstructor
public class Faculty {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String code;

	@ManyToOne
	@JoinColumn(name = "dean_id")
	private Teacher dean;

	@Column(name = "logo_url")
	private String logoUrl;

	public Long getDeanId() {
		return dean != null ? dean.getId() : null;
	}
}