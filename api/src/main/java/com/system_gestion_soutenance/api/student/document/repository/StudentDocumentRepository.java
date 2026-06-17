package com.system_gestion_soutenance.api.student.document.repository;

import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {
	List<StudentDocument> findByStudentId(Long studentId);

	Page<StudentDocument> findByStudentId(Long studentId, Pageable pageable);
}
