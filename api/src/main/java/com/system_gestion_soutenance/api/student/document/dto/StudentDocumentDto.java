package com.system_gestion_soutenance.api.student.document.dto;

import java.time.LocalDateTime;

public record StudentDocumentDto(Long id, Long studentId, String name, String type, String deadline, String status,
		LocalDateTime submittedAt, String filePath) {
}
