package com.system_gestion_soutenance.api.coordinator.defense.repository;

import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DefenseRepository extends JpaRepository<Defense, Long> {
	Optional<Defense> findByProject(Project project);

	boolean existsByProject_Id(Long projectId);

	boolean existsByMembers_Teacher_Id(Long teacherId);

	long countByMembers_Teacher_Id(Long teacherId);
}
