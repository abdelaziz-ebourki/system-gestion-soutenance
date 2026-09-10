package com.system_gestion_soutenance.api.coordinator.report.repository;

import com.system_gestion_soutenance.api.coordinator.report.entity.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, Long> {
}
