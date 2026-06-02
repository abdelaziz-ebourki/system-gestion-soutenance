package com.system_gestion_soutenance.api.coordinator.jury.repository;

import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JuryRepository extends JpaRepository<Jury, Long> {
	List<Jury> findByProjectId(Long projectId);

	@Query("SELECT DISTINCT j FROM Jury j LEFT JOIN FETCH j.project LEFT JOIN FETCH j.members m LEFT JOIN FETCH m.teacher")
	List<Jury> findAllWithDetails();
}
