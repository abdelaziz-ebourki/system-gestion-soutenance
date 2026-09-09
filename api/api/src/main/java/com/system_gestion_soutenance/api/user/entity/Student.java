package com.system_gestion_soutenance.api.user.entity;

import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("STUDENT")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Student extends User {
	private String cne;

	@Column(unique = true, nullable = true)
	private String codeApogee;

	@ManyToOne
	@JoinColumn(name = "major_id")
	private Major major;

	@ManyToOne
	@JoinColumn(name = "level_id")
	private Level level;
}
