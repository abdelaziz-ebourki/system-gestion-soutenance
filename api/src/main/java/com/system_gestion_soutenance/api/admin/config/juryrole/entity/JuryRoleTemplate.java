package com.system_gestion_soutenance.api.admin.config.juryrole.entity;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseType;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jury_role_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JuryRoleTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "defense_type", nullable = false)
	private DefenseType defenseType;

	@ElementCollection
	@CollectionTable(name = "jury_role_template_roles", joinColumns = @JoinColumn(name = "jury_role_template_id"))
	private List<TemplateRole> roles = new ArrayList<>();
}
