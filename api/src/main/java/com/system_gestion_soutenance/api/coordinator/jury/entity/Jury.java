package com.system_gestion_soutenance.api.coordinator.jury.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jury")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Jury {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@ManyToOne
	@JoinColumn(name = "jury_role_template_id", nullable = false)
	@JsonIgnore
	private JuryRoleTemplate template;

	@OneToMany(mappedBy = "jury", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<JuryMember> members = new ArrayList<>();

	public Long getTemplateId() {
		return template != null ? template.getId() : null;
	}

	public String getTemplateName() {
		return template != null ? template.getName() : null;
	}
}
