package com.system_gestion_soutenance.api.admin.config.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentConfig {

	@Id
	private Long id = 1L;

	@Column(name = "max_file_size_mb")
	private int maxFileSizeMb;

	@Column(name = "allowed_extensions")
	private String allowedExtensions;

	@Column(name = "version_limit")
	private int versionLimit;
}
