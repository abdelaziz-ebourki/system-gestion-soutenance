package com.system_gestion_soutenance.api.admin.faculty.repository;

import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
	Optional<Faculty> findByName(String name);
}
