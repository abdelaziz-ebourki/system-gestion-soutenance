package com.system_gestion_soutenance.api.admin.config.juryrole.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRole {
	private String name;
	private int count;
	private int coefficient;
}
