package com.system_gestion_soutenance.api.admin.config.level.repository;

import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LevelRepository extends JpaRepository<Level, Long> {
	Optional<Level> findByName(String name);
}
