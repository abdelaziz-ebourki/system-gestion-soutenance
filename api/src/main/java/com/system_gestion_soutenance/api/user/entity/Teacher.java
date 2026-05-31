package com.system_gestion_soutenance.api.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("TEACHER")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Teacher extends User {
	@ManyToOne
	@JoinColumn(name = "grade_id")
	private Grade grade;

	@ManyToOne
	@JoinColumn(name = "department_id")
	@JsonIgnoreProperties("head")
	private Department department;
}
