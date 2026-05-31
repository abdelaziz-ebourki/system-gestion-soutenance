package com.system_gestion_soutenance.api.admin.config.grade.repository;

import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, Long> {
	Optional<Grade> findByName(String name);
}
