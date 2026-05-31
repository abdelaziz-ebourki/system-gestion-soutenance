package com.system_gestion_soutenance.api.admin.config.general.repository;

import com.system_gestion_soutenance.api.admin.config.general.entity.GeneralSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneralSettingsRepository extends JpaRepository<GeneralSettings, Long> {
}
