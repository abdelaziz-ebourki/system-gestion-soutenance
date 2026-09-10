package com.system_gestion_soutenance.api.admin.config.teacherrank.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
@SuppressWarnings("PMD")

@Entity
@BatchSize(size = 20)
@Table(name = "teacher_rank")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRank {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
}
