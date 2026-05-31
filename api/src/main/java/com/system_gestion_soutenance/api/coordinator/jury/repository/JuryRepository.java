package com.system_gestion_soutenance.api.coordinator.jury.repository;

import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JuryRepository extends JpaRepository<Jury, Long> {
	List<Jury> findByProjectId(Long projectId);
}
