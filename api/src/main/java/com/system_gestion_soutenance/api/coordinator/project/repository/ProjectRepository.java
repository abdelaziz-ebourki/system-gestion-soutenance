package com.system_gestion_soutenance.api.coordinator.project.repository;

import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {
	List<Project> findBySupervisorId(Long supervisorId);

	@Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.supervisor")
	List<Project> findAllWithDetails();

	@Query(value = "SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.supervisor", countQuery = "SELECT COUNT(p) FROM Project p")
	Page<Project> findAllWithDetails(Pageable pageable);
}
