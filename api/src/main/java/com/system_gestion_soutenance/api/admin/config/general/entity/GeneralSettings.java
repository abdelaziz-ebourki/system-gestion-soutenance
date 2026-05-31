package com.system_gestion_soutenance.api.admin.config.general.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "general_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeneralSettings {

	@Id
	private Long id = 1L;

	@Column(name = "institution_name")
	private String institutionName;

	@Column(name = "institution_logo_url")
	private String institutionLogoUrl;

	private String timezone;

	@Column(name = "date_format")
	private String dateFormat;

	@Column(name = "setup_completed")
	private boolean setupCompleted;
}
