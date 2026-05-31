package com.system_gestion_soutenance.api.admin.config.email.repository;

import com.system_gestion_soutenance.api.admin.config.email.entity.EmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailConfigRepository extends JpaRepository<EmailConfig, Long> {
}
