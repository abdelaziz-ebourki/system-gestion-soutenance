package com.system_gestion_soutenance.api.admin.config.settings.defense.repository;

import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefenseSettingsRepository extends JpaRepository<DefenseSettings, Long> {
}
