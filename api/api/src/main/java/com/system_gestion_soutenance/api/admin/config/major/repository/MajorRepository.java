package com.system_gestion_soutenance.api.admin.config.major.repository;

import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorRepository extends JpaRepository<Major, Long> {
	Optional<Major> findByName(String name);
}
