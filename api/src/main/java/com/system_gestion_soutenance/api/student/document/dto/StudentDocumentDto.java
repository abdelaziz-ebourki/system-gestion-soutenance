package com.system_gestion_soutenance.api.student.document.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
@SuppressWarnings("PMD")

public record StudentDocumentDto(Long id, Long studentId, String name, String type, LocalDate deadline, String status,
		LocalDateTime submittedAt, String filePath) {
}