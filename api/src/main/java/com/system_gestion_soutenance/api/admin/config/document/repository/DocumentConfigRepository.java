package com.system_gestion_soutenance.api.admin.config.document.repository;

import com.system_gestion_soutenance.api.admin.config.document.entity.DocumentConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentConfigRepository extends JpaRepository<DocumentConfig, Long> {
}
