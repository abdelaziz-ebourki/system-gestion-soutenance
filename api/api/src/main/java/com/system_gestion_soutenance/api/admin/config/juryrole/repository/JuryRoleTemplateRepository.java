package com.system_gestion_soutenance.api.admin.config.juryrole.repository;

import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JuryRoleTemplateRepository extends JpaRepository<JuryRoleTemplate, Long> {
	Optional<JuryRoleTemplate> findByName(String name);
}
