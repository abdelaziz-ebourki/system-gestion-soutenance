package com.system_gestion_soutenance.api.admin.config.settings.defense.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
@SuppressWarnings("PMD")

@Entity
@Table(name = "defense_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefenseSettings {

	@Id
	private Long id = 1L;

	@Column(name = "start_time")
	private String startTime;

	@Column(name = "end_time")
	private String endTime;

	@Column(name = "defense_duration")
	private int defenseDuration;

	@Column(name = "break_duration")
	private int breakDuration;

	@Column(name = "group_creation_start_date")
	private String groupCreationStartDate;

	@Column(name = "group_creation_end_date")
	private String groupCreationEndDate;
}